package oyun.memo

import com.typesafe.config.Config
import oyun.db.dsl._

final class Env(config: Config, db: oyun.db.Env) {

  private val CollectionCache = config getString "collection.cache"

  lazy val mongoCache: MongoCache.Builder = MongoCache(db(CollectionCache))
}

object Env {
  lazy val current = "memo" boot new Env(
    config = oyun.common.PlayApp loadConfig "memo",
    db = oyun.db.Env.current)
}
