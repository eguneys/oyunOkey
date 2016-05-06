package oyun.api

import com.typesafe.config.Config

final class Env(
  config: Config,
  lobbyEnv: oyun.lobby.Env,
  roundJsonView: oyun.round.JsonView,
  getMasa: oyun.game.Game => Fu[Option[oyun.masa.Masa]],
  val isProd: Boolean) {
  object Net {
    val Domain = config getString "net.domain"
    val BaseUrl = config getString "net.base_url"
    val AssetDomain = config getString "net.asset.domain"
    val AssetVersion = config getString "net.asset.version"
  }
  val PrismicApiUrl = config getString "prismic.api_url"

  object assetVersion {
    def get = Net.AssetVersion
  }

  val roundApi = new RoundApi(
    jsonView = roundJsonView,
    getMasa = getMasa
  )

  val lobbyApi = new LobbyApi(
    lobby = lobbyEnv.lobby,
    lobbyVersion = () => lobbyEnv.history.version)
}

object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp.loadConfig,
    lobbyEnv = oyun.lobby.Env.current,
    roundJsonView = oyun.round.Env.current.jsonView,
    getMasa = oyun.masa.Env.current.masa,
    isProd = oyun.common.PlayApp.isProd)
}
