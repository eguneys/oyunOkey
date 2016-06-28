package oyun.masa

import scala.concurrent.duration.FiniteDuration

final class Winners(
  mongoCache: oyun.memo.MongoCache.Builder,
  ttl: FiniteDuration) {

  // private implicit val WinnerBSONHandler =
  //   reactivemongo.bson.Macros.handler[Winner]
}
