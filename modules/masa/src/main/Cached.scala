package oyun.masa

import scala.concurrent.duration._

import oyun.memo._

private[masa] final class Cached(
  createdTtl: FiniteDuration)(implicit system: akka.actor.ActorSystem) {

  private val nameCache = new Syncache[String, Option[String]](
    name = "masa.name",
    compute = ((id: String) => MasaRepo byId id map2 { (masa: Masa) => masa.fullName }),
    default = _ => none,
    strategy = Syncache.WaitAfterUptime(20 millis),
    expireAfter = Syncache.ExpireAfterAccess(1 hour),
    logger = logger)

  def name(id: String): Option[String] = nameCache sync id

  val promotable = AsyncCache.single(
    MasaRepo.promotable,
    timeToLive = createdTtl)

}
