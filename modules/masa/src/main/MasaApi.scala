package oyun.masa

import scala.concurrent.duration._
import scala.concurrent.Promise
import play.api.libs.json._

import akka.actor._
import akka.pattern.{ ask, pipe }

import actorApi._
import oyun.common.Debouncer
import oyun.hub.actorApi.map.{ Tell }
import oyun.hub.actorApi.lobby.ReloadMasas
import oyun.hub.Sequencer
import oyun.socket.actorApi.SendToFlag
import oyun.game.{ Mode, Game }
import oyun.user.{ User, UserRepo }

import okey.Side
import makeTimeout.short

private[masa] final class MasaApi(
  scheduleJsonView: ScheduleJsonView,
  system: ActorSystem,
  sequencers: ActorRef,
  autoPairing: AutoPairing,
  perfsUpdater: PerfsUpdater,
  socketHub: ActorRef,
  renderer: ActorSelection,
  site: ActorSelection,
  lobby: ActorSelection) {

  def addMasa(setup: MasaSetup, player: PlayerRef): Fu[Masa] = {
    findCompatible(setup, player) flatMap {
      case Some(m) => fuccess(m)
      case None => createMasa(setup, player)
    }
  }

  def createMasa(setup: MasaSetup, player: PlayerRef): Fu[Masa] = {
    val variant = okey.variant.Variant orDefault setup.variant
    val realRounds = !variant.scoreFinish option setup.rounds
    val realScores = variant.scoreFinish option setup.rounds
    val masa = Masa.make(
      createdByUserId = player.userId | player.id,
      clock = MasaClock(30),
      rounds = realRounds,
      scores = realScores,
      mode = setup.mode.fold(Mode.default)(Mode.orDefault),
      allowAnon = setup.allowAnon,
      system = System.default,
      variant = variant)
    logger.info(s"Create $masa")

    MasaRepo.insert(masa) >>
      //join(masa.id, player) >>
      //join(masa.id, PlayerRef(false)) >>
      insertPlayer(masa.id, player, Side.EastSide) >>
      insertPlayer(masa.id, PlayerRef(false), Side.WestSide) >>
      insertPlayer(masa.id, PlayerRef(false), Side.NorthSide) >>
      insertPlayer(masa.id, PlayerRef(false), Side.SouthSide) inject masa
}

  private def findCompatible(setup: MasaSetup, player: PlayerRef): Fu[Option[Masa]] =
    MasaRepo findCompatible setup flatMap {
      findCompatibleIn(setup, player, _)
    }

  private def findCompatibleIn(setup: MasaSetup, player: PlayerRef, in: List[Masa]): Fu[Option[Masa]] = in match {
    case Nil => fuccess(none)
    case m +: rest => canJoin(m, player) ?? !{
      fuccess(false)
    } flatMap {
      case true => fuccess(m.some)
      case false => findCompatibleIn(setup, player, rest)
    }
  }

  def makePairings(oldMasa: Masa, seats: List[String]) {
    Sequencing(oldMasa.id)(MasaRepo.startedById) { masa =>
      masa.createPairings(masa, seats).flatMap {
        case None => funit
        case Some(pairing) => {
          //PairingRepo.insert(pairing) >> updateNbRounds(masa.id) >>
          PairingRepo.insert(pairing) >>
          autoPairing(masa, pairing) addEffect { game =>
            sendTo(masa.id, StartGame(game))
          }
        } >> funit >> featureOneOf(masa, pairing) >>- {
          oyun.mon.masa.pairing.create()
          pairingLogger.debug(s"${masa.id} ${pairing}")
        }
      }
    }
  }

  private def featureOneOf(masa: Masa, pairing: Pairing): Funit =
    masa.featuredId ?? PairingRepo.byId flatMap { curOption =>
      def switch = MasaRepo.setFeaturedGameId(masa.id, pairing.gameId)
      switch
    }

  def invite(masaId: String, side: Option[String] = None): Fu[Unit] = {
    val ref = PlayerRef(aiLevel = 1.some)
    join(masaId, ref, side)
  }

  def join(masaId: String, player: PlayerRef, side: Option[String] = None): Fu[Unit] = {
    def joinApply(masa: Masa, player: PlayerRef) = {
      PlayerRepo.join(masa.id, player.toPlayer(masa, masa.perfLens), side flatMap Side.apply) >> updateNbPlayers(masa.id) >>- {
        socketReload(masa.id)
        publish()
      }
    }

    val promise = Promise[Unit]()
    Sequencing(masaId)(MasaRepo.enterableById) { masa =>
      canJoin(masa, player).fold(
        joinApply(masa, player) >>- {
          promise success(())
        },
        fufail(s"$player cannot join masa $masaId")
      )
    }
    promise.future
  }

  def insertPlayer(masaId: String, player: PlayerRef, side: Side): Fu[Unit] = {
    def insertApply(masa: Masa, player: PlayerRef) = {
      PlayerRepo.insertPlayer(masa.id, player.toPlayer(masa, masa.perfLens), side) >> updateNbPlayers(masa.id) >>- {
        socketReload(masa.id)
        publish()
      }
    }

    val promise = Promise[Unit]()
    Sequencing(masaId)(MasaRepo.enterableById) { masa =>
      canJoin(masa, player).fold(
        insertApply(masa, player) >>- {
          promise success(())
        },
        fufail(s"$player cannot join masa $masaId")
      )
    }
    promise.future
  }

  private def canJoin(masa: Masa, player: PlayerRef): Boolean =
    masa.mode.casual.fold(
      player.user.isDefined || masa.allowAnon,
      player.user ?? { _ => true }
    ) || !player.active

  private def updateNbPlayers(masaId: String) =
    PlayerRepo countActive masaId flatMap { MasaRepo.setNbPlayers(masaId, _) }

  private def updateNbRounds(masaId: String) =
    PairingRepo countFinished masaId flatMap { MasaRepo.setNbRounds(masaId, _) }

  def withdraw(masaId: String, playerId: String): Fu[Unit] = {
    val promise = Promise[Unit]()
    Sequencing(masaId)(MasaRepo.enterableById) { 
      case masa if masa.isCreated =>
        PlayerRepo.remove(masa.id, playerId) >> updateNbPlayers(masa.id) >>- {
          funit >>-
          socketReload(masa.id) >>-
          publish()
          promise success(())
        }
      case masa if masa.isStarted =>
        PlayerRepo.withdraw(masa.id, playerId) >> updateNbPlayers(masa.id) >>- {
          (PairingRepo removePlaying masa.id) >>-
          // funit >>-
          socketReload(masa.id) >>-
          publish()
          promise success(())
        }
      case _ => funit
    }
    promise.future
  }

  def masa(game: Game): Fu[Option[Masa]] = ~{
    for {
      masaId <- game.masaId
    } yield MasaRepo byId masaId
  }

  def start(oldMasa: Masa) {
    Sequencing(oldMasa.id)(MasaRepo.createdById) { masa =>
      MasaRepo.setStatus(masa.id, Status.Started) >>-
      sendTo(masa.id, Reload) >>-
      publish()
    }
  }

  def wipe(masa: Masa): Funit =
    MasaRepo.remove(masa).void >>
      PairingRepo.removeByMasa(masa.id) >>
      PlayerRepo.removeByMasa(masa.id) >>- publish() >>- socketReload(masa.id)

  def finish(oldMasa: Masa) {
    Sequencing(oldMasa.id)(MasaRepo.startedById) { masa =>
      PairingRepo count masa.id flatMap {
        case _ => for {
          _ <- MasaRepo.setStatus(masa.id, Status.Finished)
          // _ <- PlayerRepo unWithdraw masa.id // why?
          // _ <- PairingRepo removePlaying masa.id
          winner <- PlayerRepo winner masa.id
          _ <- winner.??(p => p.userId.??(MasaRepo.setWinnerId(masa.id, _)))
        } yield {
          sendTo(masa.id, Reload)
          publish()
          updateCountAndPerfs(masa) >>- {
            PlayerRepo activePlayers masa.id foreach {
              _ foreach { updatePlayerRating(masa) }
            }
          }
        }
      }
    }
  }

  private def updatePlayerRating(masa: Masa)(player: Player): Funit =
    (masa.perfType.ifTrue(masa.rated) ?? { perfType => player.userId ?? { UserRepo.perfOf(_, perfType) } }) flatMap { perf =>
      PlayerRepo.update(masa.id, player.id) { player =>
        funit inject
          player.copy(
            ratingDiff = perf.fold(player.ratingDiff)(_.intRating - (player.rating | 0)))
      }
    }

  private def updateCountAndPerfs(masa: Masa): Funit =
    PlayerRepo.bestByMasaWithRank(masa.id).flatMap { players =>
      UserRepo.pair(players.map(_.player.userId)).flatMap {
        case (musers) => {
          val users = musers.sequenceSides

          users ?? {
            case (users) =>
              perfsUpdater.save(masa, users zip players)
          } zip (users ?? { m => (m zip players).map {
            case(user, player) =>
              incNbMasas(masa, player.rank)(user)
          }.sequenceFu void }) void
        }
      }
    }

  private def incNbMasas(masa: Masa, rank: Int)(user: User): Funit = true ?? {
    UserRepo.incNbMasas(user.id, result = rank)
  }

  def finishGame(game: Game) {
    game.masaId foreach { masaId =>
      Sequencing(masaId)(MasaRepo.startedById) { masa =>
        PairingRepo.finish(game) >> updateNbRounds(masa.id) >>
        game.seatIds.map(updatePlayer(masa)).sequenceFu.void
      }
    }
  }

  private def updatePlayer(masa: Masa)(seatId: String): Funit =
    PlayerRepo.update(masa.id, seatId) { player =>
      PairingRepo.finishedBySeatChronological(masa.id, seatId) map { pairings =>
        val sheet = masa.system.scoringSystem.sheet(masa, seatId, pairings)
        player.copy(
          score = sheet.total + (masa.scores | 0)
        ).recomputeMagicScore
      }
    }

  private val miniStandingCache = oyun.memo.AsyncCache[String, List[RankedPlayer]](
    (id: String) => PlayerRepo.bestByMasaWithRank(id),
    timeToLive = 3 second)

  def miniStanding(masaId: String, withStanding: Boolean): Fu[Option[MiniStanding]] =
    MasaRepo byId masaId flatMap {
      _ ?? { masa =>
        if (withStanding) miniStandingCache(masa.id) map { rps =>
          MiniStanding(masa, rps.some).some
        }
        else fuccess(MiniStanding(masa, none).some)
      }
    }

  def miniStanding(masaId: String, playerId: Option[String], withStanding: Boolean): Fu[Option[MiniStanding]] =
    miniStanding(masaId, withStanding)

  def fetchVisibleMasas: Fu[VisibleMasas] =
    MasaRepo.publicCreatedSorted zip
      MasaRepo.publicStarted zip
      MasaRepo.finishedNotable(10) map {
        case ((created, started), finished) =>
          VisibleMasas(created, started, finished)
      }

  private def sequence(masaId: String)(work: => Funit) {
    sequencers ! Tell(masaId, Sequencer work(work))
    // (() => work)()
  }

  private def Sequencing(masaId: String)(fetch: String => Fu[Option[Masa]])(run: Masa => Funit) {
    sequence(masaId) {
      fetch(masaId) flatMap {
        case Some(m) => run(m)
        case None => fufail(s"Can't run sequence opeartion on missing masa $masaId")
      }
    }
  }

  private def socketReload(masaId: String) {
    sendTo(masaId, Reload)
  }

  private object publish {
    private val debouncer = system.actorOf(Props(new Debouncer(10 seconds, {
      (_: Debouncer.Nothing) =>
      fetchVisibleMasas foreach { vis =>
        site ! SendToFlag("masa", Json.obj(
          "t" -> "reload",
          "d" -> scheduleJsonView(vis)
        ))
      }
      MasaRepo.promotable foreach { masas =>
        renderer ? MasaTable(masas) map {
          case view: play.twirl.api.Html => ReloadMasas(view.body)
        } pipeToSelection lobby
      }
    })))
    def apply() { debouncer ! Debouncer.Nothing }
  }

  private def sendTo(masaId: String, msg: Any) {
    socketHub ! Tell(masaId, msg)
  }
}
