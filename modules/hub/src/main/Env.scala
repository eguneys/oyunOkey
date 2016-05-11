package oyun.hub

import akka.actor._
import com.typesafe.config.Config

final class Env(config: Config, system: ActorSystem) {
  object actor {
    val renderer = select("actor.renderer")
    val lobby = select("actor.lobby")
    val roundMap = select("actor.round.map")
    val chat = select("actor.chat")
  }

  object socket {
    val lobby = select("socket.lobby")
    val round = select("socket.round")
    val masa = select("socket.masa")
  }

  private def select(name: String) =
    system actorSelection ("/user/" + config.getString(name))
}

object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp loadConfig "hub",
    system = oyun.common.PlayApp.system
  )
}
