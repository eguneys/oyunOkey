package oyun.masa

import akka.actor._
import scala.concurrent.duration._

import actorApi._

private[masa] final class CreatedOrganizer(
  api: MasaApi,
  isOnline: Player => Boolean) extends Actor {

  override def preStart {
    pairingLogger.info("Start CreatedOrganizer")
    context setReceiveTimeout 15.seconds
    scheduleNext
  }

  case object Tick

  def scheduleNext =
    context.system.scheduler.scheduleOnce(2 seconds, self, Tick)

  def receive = {
    case ReceiveTimeout =>
      val msg = "masa.CreatedOrganizer timed out!"
      pairingLogger.error(msg)
      throw new RuntimeException(msg)

    case Tick =>
      val myself = self
      MasaRepo.allCreated map { masas =>
        masas foreach { masa =>
          PlayerRepo countActive masa.id foreach {
            // case 0 => api wipe masa
            case 4 => api start masa
            case _ => // ejectLeavers(masa)
          }
        }
      } andThenAnyway scheduleNext
  }
}
