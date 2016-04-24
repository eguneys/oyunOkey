package oyun.security

import org.joda.time.DateTime
import play.api.mvc.RequestHeader
import reactivemongo.bson.Macros

import oyun.db.dsl._
import oyun.common.HTTPRequest

object Store {

  // dirty
  private val coll = Env.current.storeColl

  private[security] def save(
    sessionId: String,
    userId: String,
    req: RequestHeader): Funit =
    coll.insert($doc(
      "_id" -> sessionId,
      "user" -> userId,
      "ip" -> HTTPRequest.lastRemoteAddress(req),
      "ua" -> HTTPRequest.userAgent(req).|("?"),
      "date" -> DateTime.now,
      "up" -> true
    )).void

  case class UserIdAndFingerprint(user: String, fp: Option[String])
  private implicit val UserIdAndFingerprintBSONReader = Macros.reader[UserIdAndFingerprint]

  def userIdAndFingerprint(sessionId: String): Fu[Option[UserIdAndFingerprint]] =
    coll.find(
      $doc("_id" -> sessionId, "up" -> true),
      $doc("user" -> true, "fp" -> true, "_id" -> false)
    ).uno[UserIdAndFingerprint]
}
