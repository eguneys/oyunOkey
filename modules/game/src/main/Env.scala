package oyun.game

import com.typesafe.config.Config
import oyun.common.PimpedConfig._

final class Env(
  config: Config,
  db: oyun.db.Env,
  mongoCache: oyun.memo.MongoCache.Builder) {
  private val settings = new {
    val CachedNbTtl = config duration "cached.nb.ttl"
    val CollectionGame = config getString "collection.game"
  }
  import settings._

  private[game] lazy val gameColl = db(CollectionGame)

  lazy val cached = new Cached(
    coll = gameColl,
    mongoCache = mongoCache,
    defaultTtl = CachedNbTtl)
}

object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp loadConfig "game",
    db = oyun.db.Env.current,
    mongoCache = oyun.memo.Env.current.mongoCache)
}
