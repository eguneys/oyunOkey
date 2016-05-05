package oyun.fishnet

import scala.concurrent.duration._

import akka.actor._

private[fishnet] class ControllerActor(
  repo: FishnetRepo,
  api: FishnetApi) extends Actor {

  override def preStart {
    context setReceiveTimeout 15.seconds
    scheduleNext
  }

  lazy val offlineClient = Client.offline

  def scheduleNext =
    context.system.scheduler.scheduleOnce(2 seconds, self, Acquire(offlineClient))

  case class Acquire(client: Client)

  def receive = {
    case Acquire(client) => {
      api acquireMove client map { _ ?? { workMove =>
        println(s"workmove ${workMove}")
        api.postMove(workMove.id, client, none)
      }
      }
    } andThenAnyway scheduleNext
  }

}
