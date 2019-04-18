package oyun.game

import akka.actor._

import com.typesafe.config.Config
import oyun.common.PimpedConfig._

final class Env(
  config: Config,
  db: oyun.db.Env,
  asyncCache: oyun.memo.AsyncCache.Builder,
  mongoCache: oyun.memo.MongoCache.Builder,
  system: ActorSystem) {
  private val settings = new {
    val CaptcherName = config getString "captcher.name"
    val CachedNbTtl = config duration "cached.nb.ttl"
    val CollectionGame = config getString "collection.game"
  }
  import settings._

  private[game] lazy val gameColl = db(CollectionGame)

  lazy val cached = new Cached(
    coll = gameColl,
    asyncCache = asyncCache,
    mongoCache = mongoCache,
    defaultTtl = CachedNbTtl)
}

object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp loadConfig "game",
    db = oyun.db.Env.current,
    mongoCache = oyun.memo.Env.current.mongoCache,
    asyncCache = oyun.memo.Env.current.asyncCache,
    system = oyun.common.PlayApp.system)
}
