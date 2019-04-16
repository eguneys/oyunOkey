package oyun.user

import oyun.common.LightUser

import oyun.db.dsl._
import oyun.memo.Syncache
import reactivemongo.bson._
import scala.concurrent.duration._
import User.{ BSONFields => F }

final class LightUserApi(coll: Coll)(implicit system: akka.actor.ActorSystem) {
  def get(id: String): Option[LightUser] = cache sync id
  def sync(id: String): Option[LightUser] = cache sync id

  def invalidate = cache invalidate _

  def preloadMany = cache preloadMany _

  private implicit val lightUserReader = new BSONDocumentReader[LightUser] {
    def read(doc: BSONDocument) = LightUser(
      id = doc.getAs[String](F.id) err "LightUser id missing",
      name = doc.getAs[String](F.username) err "LightUser username missing",
      title = none)
  }

  private val cacheName = "user.light"

  private val cache = new Syncache[String, Option[LightUser]](
    name = cacheName,
    compute = id => coll.find($id(id)).uno[LightUser],
    default = id => LightUser(id, id, none).some,
    strategy = Syncache.WaitAfterUptime(10 millis),
    expireAfter = Syncache.ExpireAfterAccess(15 minutes),
    logger = logger branch "LightUserApi"
  )
}

object LightUserApi {

}
