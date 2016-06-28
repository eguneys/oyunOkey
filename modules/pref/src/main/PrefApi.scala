package oyun.pref

import play.api.libs.json.Json
import scala.concurrent.duration.Duration

import reactivemongo.bson._

import oyun.db.BSON
import oyun.db.dsl._
import oyun.memo.AsyncCache
import oyun.user.User

final class PrefApi(
  coll: Coll,
  cacheTtl: Duration,
  bus: oyun.common.Bus) {

  private def fetchPref(id: String): Fu[Option[Pref]] = coll.find(BSONDocument("_id" -> id)).uno[Pref]
  private val cache = AsyncCache(fetchPref, timeToLive = cacheTtl)


  private implicit val prefBSONHandler = new BSON[Pref] {

    def reads(r: BSON.Reader): Pref = Pref(
      _id = r str "_id",
      theme = r.getD("theme", Pref.default.theme))

    def writes(w: BSON.Writer, o: Pref) = BSONDocument(
      "_id" -> o._id,
      "theme" -> o.theme)

  }

  def getPrefById(id: String): Fu[Pref] = cache(id) map (_ getOrElse Pref.create(id))
  val getPref = getPrefById _
  def getPref(user: User): Fu[Pref] = getPref(user.id)

}
