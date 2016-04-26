package oyun.masa

import akka.actor._
import scala.concurrent.duration._

import actorApi._

private[masa] final class CreatedOrganizer(
  api: MasaApi,
  isOnline: String => Player => Fu[Boolean]) extends Actor {

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
            case 0 if !masa.isRecentlyCreated => api wipe masa
            case 4 => api start masa
            case _ if !masa.isRecentlyCreated => ejectLeavers(masa)
            case _ => funit
          }
        }
        oyun.mon.masa.created(masas.size)
      } andThenAnyway scheduleNext
  }

  private def ejectLeavers(masa: Masa) =
    PlayerRepo allByMasa masa.id foreach {
      _ foreach { p => isOnline(masa.id)(p) map {
        !_ ! api.withdraw(masa.id, p.id)
      } }
    }
}
