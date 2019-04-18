package oyun.lobby

import scala.concurrent.duration._

import akka.actor.ActorSystem
import play.api.libs.json._
import play.api.libs.iteratee._
import play.twirl.api.Html

import actorApi._
import oyun.common.PimpedJson._
import oyun.hub.actorApi.lobby._
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.socket.{ SocketTrouper }

private[lobby] final class LobbySocket(
  system: ActorSystem,
  uidTtl: FiniteDuration) extends SocketTrouper[Member](system, uidTtl) {

  system.oyunBus.subscribe(this, 'lobbySocket)

  def receiveSpecific = {

    // case PingVersion(uid, v) => Future {
    //   ping(uid)
    //   withMember(uid) { m =>
    //     history.since(v).fold {
    //       resync(m)
    //     }(_ foreach sendMessage(m))
    //   }
    // }

    case Join(uid, user, promise) =>
      val (enumerator, channel) = Concurrent.broadcast[JsValue]
      val member = Member(channel, user, uid)
      addMember(uid, member)
      promise success Connected(enumerator, member)

    case ReloadMasas(html) => notifyAllAsync(makeMessage("masas", html))

    case AddHook(hook) =>
      // notifyVersion("had", hook.render)

    case RemoveHook(hookId) =>
      // notifyVersion("hrm", hookId)

    case UpdateHook(hook) => 
      // notifyVersion("hup", hook.render)

    case JoinHook(uid, challengeId, side) =>
      withMember(uid)(notifyPlayerJoin(challengeId, side))

  }

  private def notifyPlayerJoin(challengeId: String, side: okey.Side) = { member: Member =>
    notifyMember("redirect", Json.obj(
      "id" -> challengeId,
      "url" -> playerUrl(challengeId, side)
    ).noNull)(member)
  }
  private def playerUrl(challId: String, side: okey.Side) = s"/$challId/$side.name"
}
