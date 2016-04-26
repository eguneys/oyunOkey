package oyun.masa

import scala.concurrent.duration._
import scala.concurrent.Promise

import akka.actor.{ ActorRef }
import akka.pattern.ask

import actorApi._
import oyun.hub.actorApi.map.{ Tell }
import oyun.hub.Sequencer
import oyun.game.{ Game }


import okey.Side

private[masa] final class MasaApi(
  sequencers: ActorRef,
  autoPairing: AutoPairing,
  socketHub: ActorRef
) {

  def createMasa(setup: MasaSetup, player: PlayerRef): Fu[Masa] = {
    val variant = okey.variant.Variant orDefault setup.variant
    val masa = Masa.make(
      createdByUserId = player.id,
      rounds = setup.rounds,
      system = System.Arena,
      variant = variant)
    logger.info(s"Create $masa")

    MasaRepo.insert(masa) >> join(masa.id, player) inject masa
  }

  def makePairings(oldMasa: Masa, players: List[String]) {
    Sequencing(oldMasa.id)(MasaRepo.startedById) { masa =>
      masa.createPairings(masa, players).flatMap {
        case None => funit
        case Some(pairing) => {
          //PairingRepo.insert(pairing) >> updateNbRounds(masa.id) >>
          PairingRepo.insert(pairing) >>
            autoPairing(masa, pairing) addEffect { game =>
              sendTo(masa.id, StartGame(game))
            }
        } >> funit >>- {
          oyun.mon.masa.pairing.create()
          pairingLogger.debug(s"${masa.id} ${pairing}")
        }
      }
    }
  }


  def join(masaId: String, player: PlayerRef, side: Option[String] = None): Fu[Unit] = {
    val promise = Promise[Unit]()
    Sequencing(masaId)(MasaRepo.enterableById) { masa =>
      PlayerRepo.join(masa.id, player.toPlayer(masa.id), side flatMap Side.apply) >> updateNbPlayers(masa.id) >>- {
        socketReload(masa.id)
        promise success()
      }
    }
    promise.future
  }

  private def updateNbPlayers(masaId: String) =
    PlayerRepo countActive masaId flatMap { MasaRepo.setNbPlayers(masaId, _) }

  private def updateNbRounds(masaId: String) =
    PairingRepo count masaId flatMap { MasaRepo.setNbRounds(masaId, _) }

  def withdraw(masaId: String, playerId: String): Fu[Unit] = {
    val promise = Promise[Unit]()
    Sequencing(masaId)(MasaRepo.enterableById) { masa =>
      PlayerRepo.withdraw(masa.id, playerId) >> updateNbPlayers(masa.id) >>- {
        socketReload(masa.id)
        promise success()
      }
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
      sendTo(masa.id, Reload)
    }
  }

  def wipe(masa: Masa): Funit =
    MasaRepo.remove(masa).void >>
      PairingRepo.removeByMasa(masa.id) >>
      PlayerRepo.removeByMasa(masa.id) >>- socketReload(masa.id)

  def finish(oldMasa: Masa) {
    Sequencing(oldMasa.id)(MasaRepo.startedById) { masa =>
      PairingRepo count masa.id flatMap {
        case _ => for {
          _ <- MasaRepo.setStatus(masa.id, Status.Finished)
          // _ <- PlayerRepo unWithdraw masa.id // why?
          // _ <- PairingRepo removePlaying masa.id
        } yield {
          sendTo(masa.id, Reload)
        }
      }
    }
  }

  def finishGame(game: Game) {
    game.masaId foreach { masaId =>
      Sequencing(masaId)(MasaRepo.startedById) { masa =>
        PairingRepo.finish(game) >> updateNbRounds(masa.id) >>
        game.playerIds.map(updatePlayer(masa)).sequenceFu.void
      }
    }
  }

  private def updatePlayer(masa: Masa)(playerId: String): Funit =
    PlayerRepo.update(masa.id, playerId) { player =>
      PairingRepo.finishedByPlayerChronological(masa.id, playerId) map { pairings =>
        val sheet = masa.system.scoringSystem.sheet(masa, playerId, pairings)
        player.copy(
          score = sheet.total
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

  private def sendTo(masaId: String, msg: Any) {
    socketHub ! Tell(masaId, msg)
  }
}
