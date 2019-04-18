package oyun.masa

import scala.concurrent.duration._

import oyun.memo._

private[masa] final class Cached(
  asyncCache: oyun.memo.AsyncCache.Builder,
  createdTtl: FiniteDuration)(implicit system: akka.actor.ActorSystem) {

  private val nameCache = new Syncache[String, Option[String]](
    name = "masa.name",
    compute = ((id: String) => MasaRepo byId id map2 { (masa: Masa) => masa.fullName }),
    default = _ => none,
    strategy = Syncache.WaitAfterUptime(20 millis),
    expireAfter = Syncache.ExpireAfterAccess(1 hour),
    logger = logger)

  def name(id: String): Option[String] = nameCache sync id

  val promotable = asyncCache.single(
    name = "masa.promotable",
    MasaRepo.promotable,
    expireAfter = _.ExpireAfterWrite(createdTtl))

}
