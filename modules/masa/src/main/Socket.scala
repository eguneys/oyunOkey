package oyun.masa

import akka.actor._
import akka.pattern.pipe
import play.api.libs.json._
import play.api.libs.iteratee._

import actorApi._
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.socket.{ SocketActor, History, Historical }

private[oyun] final class Socket(
  masaId: String,
  val history: History) extends SocketActor[Member] with Historical[Member] {
  def receiveSpecific = {
    case PingVersion(uid, v) => {
      ping(uid)
    }

    case GetVersion => sender ! history.version

    case Join(uid, user) =>
      val (enumerator, channel) = Concurrent.broadcast[JsValue]
      val member = Member(channel, user)
      addMember(uid, member)
      sender ! Connected(enumerator, member)
  }
}
