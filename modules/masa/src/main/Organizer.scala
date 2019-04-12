package oyun.masa

import akka.actor._
import akka.pattern.{ ask }

import actorApi._

private[masa] final class Organizer(
  api: MasaApi,
  isOnline: Player => Boolean,
  socketHub: ActorRef) extends Actor {

  def receive = {
    case AllCreatedMasas => MasaRepo.allCreated foreach { masas =>
      masas foreach { masa =>
        PlayerRepo countActive masa.id foreach {
          case 4 => api start masa
          case _ => // ejectLeavers(masa)
        }
      }
    }
    case StartedMasas =>
      MasaRepo.started.map { started =>
        started.map { masa =>
          PlayerRepo activePlayerIds masa.id map { activePlayerIds =>
            startPairing(masa, activePlayerIds)
          }
        }
      }
  }

  private def ejectLeavers(masa: Masa) =
    PlayerRepo activePlayers masa.id foreach {
      _ filterNot isOnline foreach { player => api.withdraw(masa.id, player.id) }
    }


  private def startPairing(masa: Masa, activePlayerIds: List[String]) = {
    fuccess(activePlayerIds) zip PairingRepo.playingSeatIds(masa) foreach {
      case (activePlayers, playingUsers) =>
        val users = activePlayerIds filter { k => !playingUsers.contains(k) }
        api.makePairings(masa, users)
    }
  }
}
