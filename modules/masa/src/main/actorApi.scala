package oyun.masa
package actorApi

import scala.concurrent.Promise

import oyun.socket.Socket.Uid
import oyun.socket.SocketMember
import oyun.game.Game
import oyun.user.User

private[masa] case class Member(
  channel: JsChannel,
  userId: Option[String],
  playerId: Option[String],
  troll: Boolean) extends SocketMember

private[masa] object Member {
  def apply(channel: JsChannel, user: Option[User], player: Option[Player]): Member = Member(
    channel = channel,
    userId = user map (_.id),
    playerId = player map (_.id),
    troll = false
  )
}

private[masa] case class Join(
  uid: Uid, 
  user: Option[User],
  player: Option[Player],
  promise: Promise[Connected])
private[masa] case object Reload
private[masa] case class StartGame(game: Game)
private[masa] case class Connected(enumerator: JsEnumerator, member: Member)

case class RemindMasa(masa: Masa, activeUserIds: List[String])
case class MasaTable(masas: List[Masa])

// organizer
private[masa] case object AllCreatedMasas
private[masa] case object StartedMasas

private[masa] case object NotifyReload

private[masa] case class GetWaitingPlayers(promise: Promise[String])
