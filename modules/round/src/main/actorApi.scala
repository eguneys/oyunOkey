package oyun.round
package actorApi

import okey.Side

import oyun.socket.SocketMember
import oyun.user.User

sealed trait Member extends SocketMember {
  val side: Side
  val playerIdOption: Option[String]
}

object Member {
  def apply(
    channel: JsChannel,
    user: Option[User],
    side: Side,
    playerIdOption: Option[String]): Member = {
    val userId = user map (_.id)
    playerIdOption.fold[Member](Watcher(channel, userId, side)) { playerId =>
      Owner(channel, userId, playerId, side)
    }
  }
}

case class Owner(
  channel: JsChannel,
  userId: Option[String],
  playerId: String,
  side: Side) extends Member {
  val playerIdOption = playerId.some
}

case class Watcher(
  channel: JsChannel,
  userId: Option[String],
  side: Side) extends Member {
  val playerIdOption = None
}

case class Join(
  uid: String,
  user: Option[User],
  side: Side,
  playerId: Option[String])
case class Connected(enumerator: JsEnumerator, member: Member)

case object GetSocketStatus
case class SocketStatus(version: Int)
