package oyun.masa

import akka.actor.{ ActorRef }
import akka.pattern.ask

import actorApi._
import oyun.hub.actorApi.map.{ Tell }

import okey.Side

private[masa] final class MasaApi(
  socketHub: ActorRef
) {

  def createMasa(setup: MasaSetup, player: PlayerRef): Fu[Masa] = {
    val masa = Masa.make()

    MasaRepo.insert(masa) >>- join(masa.id, player) inject masa
  }


  def join(masaId: String, player: PlayerRef, side: Option[String] = None) {
    Sequencing(masaId)(MasaRepo.enterableById) { masa =>
      PlayerRepo.join(masa.id, player.toPlayer(masa.id), side map Side.apply) >>- {
        socketReload(masa.id)
      }
    }
  }

  def withdraw(masaId: String, playerId: String) {
    Sequencing(masaId)(MasaRepo.enterableById) { masa =>
      PlayerRepo.withdraw(masa.id, playerId) >>- socketReload(masa.id)
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
