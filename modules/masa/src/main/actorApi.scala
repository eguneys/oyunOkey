package oyun.masa
package actorApi

import oyun.socket.SocketMember
import oyun.game.Game
import oyun.user.User

private[masa] case class Member(
  channel: JsChannel,
  userId: Option[String],
  troll: Boolean) extends SocketMember

private[masa] object Member {
  def apply(channel: JsChannel, user: Option[User]): Member = Member(
    channel = channel,
    userId = user map (_.id),
    troll = false
  )
}

private[masa] case class Join(
  uid: String, 
  user: Option[User])
private[masa] case object Reload
private[masa] case class StartGame(game: Game)
private[masa] case class Connected(enumerator: JsEnumerator, member: Member)

// organizer
private[masa] case object AllCreatedMasas
private[masa] case object StartedMasas

private[masa] case object NotifyReload
