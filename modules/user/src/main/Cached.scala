package oyun.user

import scala.concurrent.duration._
import reactivemongo.bson._

import oyun.db.BSON
import oyun.db.dsl._
import oyun.memo.{ ExpireSetMemo, MongoCache }
import oyun.rating.{ Perf, PerfType }
import oyun.common.LightUser
import User.{ LightPerf, LightCount }

final class Cached(
  userColl: Coll,
  nbTtl: FiniteDuration,
  onlineUserIdMemo: ExpireSetMemo,
  mongoCache: MongoCache.Builder,
  rankingApi: RankingApi) {

  private val countCache = mongoCache.single[Int](
    prefix = "user:nb",
    f = userColl.count(UserRepo.enabledSelect.some),
    timeToLive = nbTtl)

  def countEnabled: Fu[Int] = countCache(true)

  private implicit val LightUserBSONHandler = Macros.handler[LightUser]
  private implicit val LightPerfBSONHandler = Macros.handler[LightPerf]
  private implicit val LightCountBSONHandler = Macros.handler[LightCount]

  def leaderboards: Fu[Perfs.Leaderboards] = for {
    yuzbir <- top10Perf(PerfType.Yuzbir.id)
  } yield Perfs.Leaderboards(
    yuzbir = yuzbir)

  val top10Perf = mongoCache[Perf.ID, List[LightPerf]](
    prefix = "user:top10:perf",
    f = (perf: Perf.ID) => rankingApi.topPerf(perf, 10),
    timeToLive = 10 seconds,
    keyToString = _.toString)

  val topNbGame= mongoCache[Int, List[User.LightCount]](
    prefix = "user:top:nbGame",
    f = nb => {
      val l = UserRepo topNbGame nb map { _ map (_.lightCount) }
      println("topnbgame", l)
      l
    },
    timeToLive = 34 minutes,
    // timeToLive = 1 seconds,
    keyToString = _.toString)

  val top50Online = oyun.memo.AsyncCache.single[List[User]](
    f = UserRepo.byIdsSortRating(onlineUserIdMemo.keys, 50),
    timeToLive = 10 seconds)

}
