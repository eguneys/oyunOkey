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
      notifyAll("redirect", game.id)

    case Reload => notifyReload

    case PingVersion(uid, v) => {
      ping(uid)
      timeBomb.delay
    }

    case Broom => {
      broom
      if (timeBomb.boom) self ! PoisonPill
    }

    case GetVersion => sender ! history.version

    case Join(uid, user) =>
      val (enumerator, channel) = Concurrent.broadcast[JsValue]
      val member = Member(channel, user)
      addMember(uid, member)
      sender ! Connected(enumerator, member)


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
}
