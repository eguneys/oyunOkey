package oyun.lobby

import play.api.libs.json._
import org.joda.time.DateTime
import ornicar.scalalib.Random

import oyun.user.{ User }
import oyun.socket.Socket.Uid

case class Hook(
  id: String,
  uid: Uid, // owner socket uid
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
    uid: Uid,
    sid: Option[String],
    user: Option[User]) = new Hook(
      id = Random nextString idSize,
      uid = uid,
      sid = sid,
      user = user map { LobbyUser.make },
      createdAt = DateTime.now)
}
