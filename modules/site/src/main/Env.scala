package oyun.site

import akka.actor._
import com.typesafe.config.Config
import oyun.common.PimpedConfig._


final class Env(
  config: Config,
  hub: oyun.hub.Env,
  system: ActorSystem) {

  private val SocketUidTtl = config duration "socket.uid.ttl"
  private val SocketName = config getString "socket.name"

  private val socket = system.actorOf(
    Props(new Socket(timeout = SocketUidTtl)), name = SocketName)

  lazy val socketHandler = new SocketHandler(socket, hub)

}

object Env {
  lazy val current = "site" boot new Env(
    config = oyun.common.PlayApp loadConfig "site",
    hub = oyun.hub.Env.current,
    system = oyun.common.PlayApp.system)
}
