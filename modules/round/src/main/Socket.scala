package oyun.round

import akka.actor._
import akka.pattern.{ ask, pipe }
import play.api.libs.iteratee._
import play.api.libs.json._

import actorApi._
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.socket._
import okey.{ Sides, Side }


private[round] final class Socket(history: History) extends SocketActor[Member] {


  private final class Player(side: Side) {
    def isGone = fuccess(false)
  }

  private val players = Sides(side => new Player(side))

  def receiveSpecific = {
    case PingVersion(uid, v) =>
      ping(uid)

    case GetSocketStatus =>
      playersGet(_.isGone).sequenceSides map { sidesIsGone =>

        val sidesOnGame = players sideMap { case (side, p) => ownerOf(side).isDefined }
        SocketStatus(
          version = history.getVersion,
          sidesIsGone = sidesIsGone,
          sidesOnGame = sidesOnGame)
      } pipeTo sender

    case Join(uid, user, side, playerId) =>
      val (enumerator, channel) = Concurrent.broadcast[JsValue]
      val member = Member(channel, user, side, playerId)
      addMember(uid, member)
      sender ! Connected(enumerator, member)
  }

  def ownerOf(side: Side): Option[Member] =
    members.values find { m => m.owner && m.side == side }

  private def playersGet[A](getter: Player => A): Sides[A] = players map getter
}
