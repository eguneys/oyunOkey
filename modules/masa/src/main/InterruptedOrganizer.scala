package oyun.masa

import akka.actor._
import scala.concurrent.duration._

import actorApi._

private[masa] final class InterruptedOrganizer(
  api: MasaApi) extends Actor {

  override def preStart {
    pairingLogger.info("Start InterruptedOrganizer")
    context setReceiveTimeout 15.seconds
    scheduleNext
  }

  case object Tick

  def scheduleNext =
    context.system.scheduler.scheduleOnce(2 seconds, self, Tick)

  def receive = {
    case ReceiveTimeout =>
      val msg = "masa.InterruptedOrganizer timed out!"
      pairingLogger.error(msg)
      throw new RuntimeException(msg)

    case Tick =>
      val myself = self
      MasaRepo.allInterrupted map { masas =>
        masas foreach { masa =>
          PlayerRepo countActive masa.id foreach {
            case 0 if !masa.isRecentlyCreated => api wipe masa
            case 4 => api resume masa
            case nb if masa.hasWaitedEnough =>
              api wipe masa
            // case _ if !masa.isRecentlyCreated => ejectLeavers(masa)
            case _ => funit
          }
        }
        oyun.mon.masa.interrupted(masas.size)
      } andThenAnyway scheduleNext
  }

  // private def ejectLeavers(masa: Masa) =
  //   PlayerRepo allByMasa masa.id foreach {
  //     _ foreach { p => isOnline(masa.id)(p) map {
  //       !_ ! api.withdraw(masa.id, p.id)
  //     } }
  //   }
}
