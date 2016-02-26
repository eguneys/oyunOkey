package oyun.lobby
package actorApi

import oyun.socket.SocketMember
import oyun.user.User

private[lobby] case class LobbyUser(
  id: String, 
  username: String)

private[lobby] object LobbyUser {
  def make(user: User) = LobbyUser(
    id = user.id,
    username = user.username)
}

private[lobby] case class Member(
  channel: JsChannel,
  user: Option[LobbyUser],
  uid: String) extends SocketMember {
  val userId = user map (_.id)
}

private[lobby] object Member {
  def apply(channel: JsChannel, user: Option[User], uid: String, blocking: Set[String] = Set.empty): Member = Member(
    channel = channel,
    user = user map { LobbyUser.make(_) },
    uid = uid)
}

private[lobby] case class Connected(enumerator: JsEnumerator, member: Member)
private[lobby] case class SaveHook(msg: AddHook)
private[lobby] case class RemoveHook(hookId: String)
private[lobby] case class BiteHook(hookId: String, uid: String, user: Option[LobbyUser])
private[lobby] case class Join(uid: String, user: Option[User])

case class AddHook(hook: Hook)
case class HooksFor(user: Option[User])
