package oyun.user

import oyun.db.dsl._
import oyun.memo.{ AsyncCache, MongoCache }
import oyun.rating.{ Perf, PerfType }


final class RankingApi(
  coll: Coll,
  mongoCache: MongoCache.Builder,
  lightUser: String => Option[oyun.common.LightUser]) {

  import RankingApi._
  private implicit val rankingBSONHandler = reactivemongo.bson.Macros.handler[Ranking]

  def topPerf(perfId: Perf.ID, nb: Int): Fu[List[User.LightPerf]] =
    PerfType.id2key(perfId) ?? { perfKey =>
      coll.find($doc("perf" -> perfId))
        .sort($doc("rating" -> -1))
        .cursor[Ranking]().gather[List](nb) map {
        _.flatMap { r =>
          lightUser(r.user) map { light =>
            User.LightPerf(
              user = light,
              perfKey = perfKey,
              rating = r.rating,
              progress = ~r.prog)
          }
        }
      }
    }
}

object RankingApi {

  private case class Ranking(user: String, rating: Int, prog: Option[Int])

}
