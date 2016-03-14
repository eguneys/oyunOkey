package oyun.masa

import akka.actor.{ ActorRef }
import akka.pattern.ask

import actorApi._
import oyun.hub.actorApi.map.{ Tell }

import okey.Side

private[masa] final class MasaApi(
  autoPairing: AutoPairing,
  socketHub: ActorRef
) {

  def createMasa(setup: MasaSetup, player: PlayerRef): Fu[Masa] = {
    val masa = Masa.make()

    MasaRepo.insert(masa) >>- join(masa.id, player) inject masa
  }

  def makePairings(oldMasa: Masa, players: List[String]) {
    Sequencing(oldMasa.id)(MasaRepo.startedById) { masa =>
      masa.createPairings(masa, players).flatMap {
        case None => funit
        case Some(pairing) => {
          PairingRepo.insert(pairing) >>
            autoPairing(masa, pairing) addEffect { game =>
              sendTo(masa.id, StartGame(game))
            }
        } >> funit
      }
    }
  }


  def join(masaId: String, player: PlayerRef, side: Option[String] = None) {
    Sequencing(masaId)(MasaRepo.enterableById) { masa =>
      PlayerRepo.join(masa.id, player.toPlayer(masa.id), side flatMap Side.apply) >>- {
        socketReload(masa.id)
      }
    }
  }

  def withdraw(masaId: String, playerId: String) {
    Sequencing(masaId)(MasaRepo.enterableById) { masa =>
      PlayerRepo.withdraw(masa.id, playerId) >>- socketReload(masa.id)
    }
  }


  def start(oldMasa: Masa) {
    Sequencing(oldMasa.id)(MasaRepo.createdById) { masa =>
      MasaRepo.setStatus(masa.id, Status.Started) >>-
      sendTo(masa.id, Reload)
    }
  }


  private def sequence(masaId: String)(work: => Funit) {
    //sequencers ! Tell(masaId, Sequencer work work)
    (() => work)()
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
