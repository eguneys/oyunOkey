package oyun.masa

import akka.actor._
import akka.pattern.pipe
import play.api.libs.json._
import play.api.libs.iteratee._
import scala.concurrent.duration._

import actorApi._
import oyun.hub.TimeBomb
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.socket.{ SocketActor, History, Historical }

private[oyun] final class Socket(
  masaId: String,
  val history: History,
  socketTimeout: Duration) extends SocketActor[Member] with Historical[Member] {

  private val timeBomb = new TimeBomb(socketTimeout)

  private var delayedReloadNotification = false

  def receiveSpecific = {
    case StartGame(game) =>
      //notifyAll("redirect", game.id)
      game.players foreach { player =>
        player.playerId foreach { playerId =>
          membersByPlayerId(playerId) foreach { member =>
            notifyMember("redirect", game.id)(member)
          }
        }
      }

    case Reload => notifyReload

    case GetWaitingPlayers =>
      val waitingPlayers = playerIds.toSet
      sender ! waitingPlayers

    case PingVersion(uid, v) => {
      ping(uid)
      timeBomb.delay
    }

    case Broom => {
      broom
      if (timeBomb.boom) self ! PoisonPill
    }

    case GetVersion => sender ! history.version

    case Join(uid, user, player) =>
      val (enumerator, channel) = Concurrent.broadcast[JsValue]
      val member = Member(channel, user, player)
      addMember(uid, member)
      sender ! Connected(enumerator, member)

    case oyun.chat.actorApi.ChatLine(_, line) => line match {
      case line: oyun.chat.UserLine =>
        notifyVersion("message", oyun.chat.Line toJson line)
      case _ =>
    }

    case NotifyReload =>
      delayedReloadNotification = false
      notifyAll("reload")
  }

  def notifyReload {
    if (!delayedReloadNotification) {
      delayedReloadNotification = true
      // keep the delay low for immediate response to join/withdraw
      // but still debounce to avoid masa start message rush
      context.system.scheduler.scheduleOnce(300 millis, self, NotifyReload)
    }
  }

  def membersByPlayerId(playerId: String): Iterable[Member] = members collect {
    case (_, member) if member.playerId.contains(playerId) => member
  }

  def playerIds: Iterable[String] = members.values.flatMap(_.playerId)
}
