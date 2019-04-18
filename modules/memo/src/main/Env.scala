package oyun.memo

import com.typesafe.config.Config
import oyun.db.dsl._

final class Env(config: Config,
  db: oyun.db.Env,
  system: akka.actor.ActorSystem) {

  private val CollectionCache = config getString "collection.cache"

  lazy val mongoCache: MongoCache.Builder = MongoCache(db(CollectionCache))

  lazy val asyncCache: AsyncCache.Builder = new AsyncCache.Builder()(system)
}

object Env {
  lazy val current = "memo" boot new Env(
    config = oyun.common.PlayApp loadConfig "memo",
    db = oyun.db.Env.current,
    system = oyun.common.PlayApp.system)
}
