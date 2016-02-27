package oyun.api

import com.typesafe.config.Config

final class Env(
  config: Config,
  lobbyEnv: oyun.lobby.Env,
  roundJsonView: oyun.round.JsonView) {
  object Net {
    //val Domain = config getString "net.domain"
    val AssetDomain = config getString "net.asset.domain"
  }

  object assetVersion {
    def get = 1
  }

  val roundApi = new RoundApi(
    jsonView = roundJsonView
  )

  val lobbyApi = new LobbyApi(
    lobby = lobbyEnv.lobby,
    lobbyVersion = () => lobbyEnv.history.version)
}

object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp.loadConfig,
    lobbyEnv = oyun.lobby.Env.current,
    roundJsonView = oyun.round.Env.current.jsonView)
}
