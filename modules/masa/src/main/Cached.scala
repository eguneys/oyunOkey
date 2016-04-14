package oyun.masa

import scala.concurrent.duration._

import oyun.memo._

private[masa] final class Cached(
  createdTtl: FiniteDuration) {

  val promotable = AsyncCache.single(
    MasaRepo.promotable,
    timeToLive = createdTtl)

}
