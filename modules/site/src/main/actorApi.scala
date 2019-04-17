package oyun.site
package actorApi

import scala.concurrent.Promise

import play.api.libs.json._

import oyun.socket.Socket.Uid
import oyun.socket.SocketMember

case class Member(
  channel: JsChannel,
  userId: Option[String],
  flag: Option[String]) extends SocketMember {

  val troll = false

  def hasFlag(f: String) = flag ?? (f ==)
}

case class Join(uid: Uid, userId: Option[String], flag: Option[String], promise: Promise[Connected])
private[site] case class Connected(enumerator: JsEnumerator, member: Member)
