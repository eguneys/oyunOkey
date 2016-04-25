package oyun.user

import oyun.common.LightUser

import oyun.db.dsl._
import reactivemongo.bson._
import scala.concurrent.duration._
import User.{ BSONFields => F }

final class LightUserApi(coll: Coll) {
  def get(id: String): Option[LightUser] = cache get id

  def invalidate = cache invalidate _

  private implicit val lightUserReader = new BSONDocumentReader[LightUser] {
    def read(doc: BSONDocument) = LightUser(
      id = doc.getAs[String](F.id) err "LightUser id missing",
      name = doc.getAs[String](F.username) err "LightUser username missing",
      title = none)
  }

  private val cache = oyun.memo.MixedCache[String, Option[LightUser]](
    id => coll.find(
      BSONDocument(F.id -> id),
      BSONDocument(F.username -> true)
  ).uno[LightUser],
    timeToLive = 20 minutes,
    default = id => LightUser(id, id, none).some,
    logger = logger branch "LightUserApi")
}
