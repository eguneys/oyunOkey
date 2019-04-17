package oyun.site

import scala.concurrent.duration.Duration

import akka.actor._
import play.api.libs.iteratee._
import play.api.libs.json._

import actorApi._
import oyun.socket._
import oyun.socket.actorApi.SendToFlag

private[site] final class Socket(
  system: akka.actor.ActorSystem,
  uidTtl: Duration) extends SocketTrouper[Member](system, uidTtl) {

  def receiveSpecific = {

    case Join(uid, username, tags, promise) => {
      val (enumerator, channel) = Concurrent.broadcast[JsValue]
      val member = Member(channel, username, tags)
      addMember(uid, member)
      // sender ! Connected(enumerator, member)
      promise success Connected(enumerator, member)
    }

    case SendToFlag(flag, message) => {
      members.values filter (_ hasFlag flag) foreach {
        _.channel push message
      }
    }
  }

}
