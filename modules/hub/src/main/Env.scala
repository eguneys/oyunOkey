package oyun.hub

import akka.actor._
import com.typesafe.config.Config

final class Env(config: Config, system: ActorSystem) {
  object actor {
    val lobby = select("actor.lobby")
  }

  object socket {
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
