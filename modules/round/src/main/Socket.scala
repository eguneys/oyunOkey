package oyun.round

import akka.actor._
import akka.pattern.{ ask }
import play.api.libs.iteratee._
import play.api.libs.json._

import actorApi._
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.socket._


private[round] final class Socket() extends SocketActor[Member] {

  def receiveSpecific = {
    case PingVersion(uid, v) =>
      ping(uid)

    case Join(uid, user, side, playerId) =>
      val (enumerator, channel) = Concurrent.broadcast[JsValue]
      val member = Member(channel, user, side, playerId)
      addMember(uid, member)
      sender ! Connected(enumerator, member)
  }
}
