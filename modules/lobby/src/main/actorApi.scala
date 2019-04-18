package oyun.lobby
package actorApi

import scala.concurrent.Promise

import oyun.socket.SocketMember
import oyun.socket.Socket.{ Uid }
import oyun.user.User

private[lobby] case class Member(
  channel: JsChannel,
  user: Option[LobbyUser],
  uid: Uid) extends SocketMember {
  val userId = user map (_.id)
}

private[lobby] object Member {
  def apply(channel: JsChannel, user: Option[User], uid: Uid, blocking: Set[String] = Set.empty): Member = Member(
    channel = channel,
    user = user map { LobbyUser.make(_) },
    uid = uid)
}

private[lobby] case class Connected(enumerator: JsEnumerator, member: Member)
private[lobby] case class SaveHook(msg: AddHook)
private[lobby] case class RemoveHook(hookId: String)
private[lobby] case class UpdateHook(hook: Hook)
private[lobby] case class RemoveHooks(hooks: Set[Hook])
private[lobby] case class CancelHook(uid: Uid)
private[lobby] case class BiteHook(hookId: String, uid: Uid, user: Option[LobbyUser])

private[lobby] case class Join(uid: Uid, user: Option[User], promise: Promise[Connected])

case class JoinHook(uid: Uid, challengeId: String, side: okey.Side)
case class AddHook(hook: Hook)
case class HooksFor(user: Option[User])
