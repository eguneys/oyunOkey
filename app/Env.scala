package oyun.app

import akka.actor._
import com.typesafe.config.Config

final class Env(
  config: Config,
  system: ActorSystem) {

  lazy val preloader = new mashup.Preload(
    lobbyApi = Env.api.lobbyApi)
}

object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp.loadConfig,
    system = oyun.common.PlayApp.system)

  def api = oyun.api.Env.current
  def setup = oyun.setup.Env.current
  def lobby = oyun.lobby.Env.current
}
