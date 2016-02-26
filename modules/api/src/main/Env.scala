package oyun.api

import com.typesafe.config.Config

final class Env(
  config: Config) {
  object Net {
    //val Domain = config getString "net.domain"
    val AssetDomain = config getString "net.asset.domain"
  }

  object assetVersion {
    def get = 1
  }
}

object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp.loadConfig)
}
