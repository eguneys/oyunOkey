package oyun.round
package actorApi

import scala.concurrent.Promise

import okey.{ Side, Sides }
import okey.format.Uci

import oyun.socket.SocketMember
import oyun.game.{ Event }
import oyun.user.User

case class EventList(events: List[Event])

sealed trait Member extends SocketMember {
  val side: Side
  val playerIdOption: Option[String]

  def owner = playerIdOption.isDefined
  def watcher = !owner
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
case class SocketStatus(
  version: Int,
  sidesOnGame: Sides[Boolean],
  sidesIsGone: Sides[Boolean]) {
  def onGame(side: Side) = sidesOnGame(side)
  def isGone(side: Side) = sidesIsGone(side)
}

case class SetGame(game: Option[oyun.game.Game])

package round {
  case class HumanPlay(
    playerId: String,
    uci: Uci,
    promise: Option[Promise[Unit]] = None)

  case object OutOfTime
  case object Abandon
  case object NoStart
}

private[round] case object GetNbRounds
private[round] case object NotifyCrowd
