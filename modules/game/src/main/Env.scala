package oyun.game

import com.typesafe.config.Config

final class Env(
  config: Config,
  db: oyun.db.Env) {
  private val settings = new {
    val CollectionGame = config getString "collection.game"
  }
  import settings._

  private[game] lazy val gameColl = db(CollectionGame)
}

object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp loadConfig "game",
    db = oyun.db.Env.current)
}
