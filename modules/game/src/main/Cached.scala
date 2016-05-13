package oyun.game

import scala.concurrent.duration._

import okey.variant.Variant
import org.joda.time.DateTime
import reactivemongo.bson.BSONDocument

import oyun.db.BSON._
import oyun.db.dsl._
import oyun.memo.{ MongoCache, AsyncCache, Builder }
import oyun.user.{ User }

final class Cached(
  coll: Coll,
  mongoCache: MongoCache.Builder,
  defaultTtl: FiniteDuration) {

  def nbPlaying(userId: String): Fu[Int] = countShortTtl(Query nowPlaying userId)


  private val countShortTtl = AsyncCache[BSONDocument, Int](
    f = (o: BSONDocument) => coll countSel o,
    timeToLive = 5.seconds)

  private val count = mongoCache(
    prefix = "game:count",
    f = (o: BSONDocument) => coll countSel o,
    timeToLive = defaultTtl,
    keyToString = oyun.db.BSON.hashDoc)

}
