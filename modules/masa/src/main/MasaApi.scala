package oyun.masa

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

    val promise = Promise[Unit]()
    MasaRepo.insert(masa) >>- join(masa.id, player, promise = promise.some)
    promise.future inject masa
  }

  def makePairings(oldMasa: Masa, players: List[String]) {
    Sequencing(oldMasa.id)(MasaRepo.startedById) { masa =>
      masa.createPairings(masa, players).flatMap {
        case None => funit
        case Some(pairing) => {
          PairingRepo.insert(pairing) >> updateNbRounds(masa.id) >>
            autoPairing(masa, pairing) addEffect { game =>
              sendTo(masa.id, StartGame(game))
            }
        } >> funit
      }
    }
  }


  def join(masaId: String, player: PlayerRef, side: Option[String] = None, promise: Option[Promise[Unit]] = None) {
    Sequencing(masaId, promise)(MasaRepo.enterableById) { masa =>
      PlayerRepo.join(masa.id, player.toPlayer(masa.id), side flatMap Side.apply) >> updateNbPlayers(masa.id) >>- {
        socketReload(masa.id)
      }
    }
  }

  private def updateNbPlayers(masaId: String) =
    PlayerRepo countActive masaId flatMap { MasaRepo.setNbPlayers(masaId, _) }

  private def updateNbRounds(masaId: String) =
    PairingRepo count masaId flatMap { MasaRepo.setNbRounds(masaId, _) }

  def withdraw(masaId: String, playerId: String) {
    Sequencing(masaId)(MasaRepo.enterableById) { masa =>
      PlayerRepo.withdraw(masa.id, playerId) >> updateNbPlayers(masa.id) >>- socketReload(masa.id)
    }
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


  def finishGame(game: Game) {
    game.masaId foreach { masaId =>
      Sequencing(masaId)(MasaRepo.startedById) { masa =>
        PairingRepo.finish(game) >>
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

  private def sequence(masaId: String, promise: Option[Promise[Unit]])(work: => Funit) {
    sequencers ! Tell(masaId, Sequencer work(work, promise))
    // (() => work)()
  }

  private def Sequencing(masaId: String, promise: Option[Promise[Unit]] = None)(fetch: String => Fu[Option[Masa]])(run: Masa => Funit) {
    sequence(masaId, promise) {
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
