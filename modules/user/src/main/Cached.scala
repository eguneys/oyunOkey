package oyun.user

import scala.concurrent.duration._

import oyun.db.dsl._
import oyun.memo.{ ExpireSetMemo, MongoCache }

final class Cached(
  userColl: Coll,
  nbTtl: FiniteDuration,
  mongoCache: MongoCache.Builder) {

  private val countCache = mongoCache.single[Int](
    prefix = "user:nb",
    f = userColl.count(UserRepo.enabledSelect.some),
    timeToLive = nbTtl)

  def countEnabled: Fu[Int] = countCache(true)
}
