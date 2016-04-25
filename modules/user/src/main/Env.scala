package oyun.user

import akka.actor._
import com.typesafe.config.Config

import oyun.common.PimpedConfig._
import oyun.memo.{ ExpireSetMemo, MongoCache }

final class Env(
  config: Config,
  db: oyun.db.Env,
  mongoCache: MongoCache.Builder,
  scheduler: oyun.common.Scheduler,
  system: ActorSystem) {

  private val settings = new {
    val CachedNbTtl = config duration "cached.nb.ttl"
    val OnlineTtl = config duration "online.ttl"
    val CollectionUser = config getString "collection.user"
  }
  import settings._

  lazy val userColl = db(CollectionUser)

  lazy val onlineUserIdMemo = new ExpireSetMemo(ttl = OnlineTtl)

  def isOnline(userId: String) = onlineUserIdMemo get userId

  def countEnabled = cached.countEnabled

  lazy val cached = new Cached(
    userColl = userColl,
    nbTtl = CachedNbTtl,
    mongoCache = mongoCache)
}


object Env {

  lazy val current: Env = "user" boot new Env(
    config = oyun.common.PlayApp loadConfig "user",
    db = oyun.db.Env.current,
    mongoCache = oyun.memo.Env.current.mongoCache,
    scheduler = oyun.common.PlayApp.scheduler,
    system = oyun.common.PlayApp.system)
}
