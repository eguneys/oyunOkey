package oyun.masa

import akka.actor._
import scala.concurrent.duration._

import actorApi._

private[masa] final class StartedOrganizer(
  api: MasaApi,
  isOnline: Player => Boolean,
  socketHub: ActorRef) extends Actor {

  override def preStart {
    pairingLogger.info("Start StartedOrganizer")
    context setReceiveTimeout 15.seconds
    scheduleNext
  }

  case object Tick

  def scheduleNext =
    context.system.scheduler.scheduleOnce(3 seconds, self, Tick)

  def receive = {
    case ReceiveTimeout =>
      val msg = "masa.StartedOrganizer timed out!"
      pairingLogger.error(msg)
      throw new RuntimeException(msg)

    case Tick =>
      val myself = self
      MasaRepo.started map { started =>
        started.map { masa =>
          PlayerRepo activePlayerIds masa.id map { activePlayerIds =>
            startPairing(masa, activePlayerIds)
          }
        }
      } andThenAnyway scheduleNext
  }

  private def startPairing(masa: Masa, activePlayerIds: List[String]) = {
    fuccess(activePlayerIds) zip PairingRepo.playingPlayerIds(masa) foreach {
      case (activePlayers, playingUsers) =>
        val users = activePlayerIds filter { k => !playingUsers.contains(k) }
        api.makePairings(masa, users)
    }
  }
}
