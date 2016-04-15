package oyun.masa

import scala.concurrent.duration._

import oyun.memo._

private[masa] final class Cached(
  createdTtl: FiniteDuration) {

  private val nameCache = MixedCache[String, Option[String]](
    ((id: String) => MasaRepo byId id map2 { (masa: Masa) => masa.fullName }),
    timeToLive = 6 hours,
    default = _ => none,
    logger = logger)

  def name(id: String): Option[String] = nameCache get id

  val promotable = AsyncCache.single(
    MasaRepo.promotable,
    timeToLive = createdTtl)

}
