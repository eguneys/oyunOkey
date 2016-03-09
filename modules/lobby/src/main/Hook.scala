package oyun.lobby

import play.api.libs.json._
import org.joda.time.DateTime
import ornicar.scalalib.Random

import actorApi.LobbyUser
import oyun.user.{ User }

case class Hook(
  id: String,
  uid: String, // owner socket uid
  sid: Option[String], // owner cookie (used to prevent multiple hooks)
  user: Option[LobbyUser],
  createdAt: DateTime) {
  def render: JsObject = Json.obj(
    "id" -> id,
    "uid" -> uid,
    "u" -> user.map(_.username)
  )
}


object Hook {
  val idSize = 8

  def make(
    uid: String,
    sid: Option[String],
    user: Option[User]) = new Hook(
      id = Random nextStringUppercase idSize,
      uid = uid,
      sid = sid,
      user = user map { LobbyUser.make },
      createdAt = DateTime.now)
}
