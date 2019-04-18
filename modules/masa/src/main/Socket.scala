package oyun.masa

import akka.actor._
import akka.pattern.pipe
import play.api.libs.json._
import play.api.libs.iteratee._
import scala.concurrent.duration._

import actorApi._
import oyun.hub.TimeBomb
import oyun.chat.Chat
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.socket.{ SocketTrouper, History, Historical }

private[oyun] final class MasaSocket(
  system: ActorSystem,
  masaId: String,
  protected val history: History,
  uidTtl: Duration) extends SocketTrouper[Member](system, uidTtl) with Historical[Member] {

  private val timeBomb = new TimeBomb(uidTtl)

  private var delayedReloadNotification = false

  private def chatClassifier = Chat classify Chat.Id(masaId)

  oyunBus.subscribe(this, chatClassifier)

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

    case Reload => 
      notifyReload

    case GetWaitingPlayers(promise) =>
      // val waitingPlayers = playerIds.toSet
      // promise success waitingPlayers

    // case PingVersion(uid, v) => {
    //   ping(uid)
    //   timeBomb.delay
    //   withMember(uid) { m =>
    //     history.since(v).fold(resync(m))(_ foreach sendMessage(m))
    //   }
    // }

    // case Broom => {
    //   broom
    //   if (timeBomb.boom) self ! PoisonPill
    // }

    case oyun.socket.Socket.GetVersion(promise) => promise success history.version

    case Join(uid, user, player, promise) =>
      val (enumerator, channel) = Concurrent.broadcast[JsValue]
      val member = Member(channel, user, player)
      addMember(uid, member)
      notifyReload
      promise success Connected(enumerator, member)

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
      system.scheduler.scheduleOnce(1 second)(this ! NotifyReload)
    }
  }

  def membersByPlayerId(playerId: String): Iterable[Member] = members collect {
    case (_, member) if member.playerId.contains(playerId) => member
  }

  def playerIds: Iterable[String] = members.values.flatMap(_.playerId)
}
