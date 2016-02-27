package oyun.round

import akka.actor._
import com.typesafe.config.Config

final class Env(
  config: Config,
  system: ActorSystem) {

  lazy val jsonView = new JsonView()
}

object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp loadConfig "round",
    system = oyun.common.PlayApp.system)
}
