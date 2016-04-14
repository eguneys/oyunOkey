package oyun.socket

import akka.actor._
import akka.pattern.{ ask }
import com.typesafe.config.Config

import actorApi._
import makeTimeout.short

final class Env(
  config: Config,
  system: ActorSystem,
  scheduler: oyun.common.Scheduler) {

  import scala.concurrent.duration._

  private val HubName = config getString "hub.name"

  private val socketHub = system.actorOf(Props[SocketHub], name = HubName)

  scheduler.once(10 seconds) {
    scheduler.message(4 seconds) { socketHub -> actorApi.Broom }
  }

}

object Env {
  lazy val current = "socket" boot new Env(
    config = oyun.common.PlayApp loadConfig "socket",
    system = oyun.common.PlayApp.system,
    scheduler = oyun.common.PlayApp.scheduler
  )
}
