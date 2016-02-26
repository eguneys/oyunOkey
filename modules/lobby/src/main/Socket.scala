package oyun.lobby

import akka.pattern.ask
import play.api.libs.json._
import play.api.libs.iteratee._

import actorApi._
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.socket.{ SocketActor, History, Historical }

private[lobby] final class Socket(
  val history: History) extends SocketActor[Member] with Historical[Member] {

  def receiveSpecific = {
    case PingVersion(uid, v) =>
      ping(uid)
    case Join(uid, user) =>
      val (enumerator, channel) = Concurrent.broadcast[JsValue]
      val member = Member(channel, user, uid)
      addMember(uid, member)
      sender ! Connected(enumerator, member)

    case AddHook(hook) =>
      notifyVersion("had", hook.render)
    case RemoveHook(hookId) => notifyVersion("hrm", hookId)
  }
}
