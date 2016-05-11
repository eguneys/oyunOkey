package oyun.lobby

import scala.concurrent.Future

import akka.pattern.ask
import play.api.libs.json._
import play.api.libs.iteratee._
import play.twirl.api.Html

import actorApi._
import oyun.common.PimpedJson._
import oyun.hub.actorApi.lobby._
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.socket.{ SocketActor, History, Historical }

private[lobby] final class Socket(
  val history: History) extends SocketActor[Member] with Historical[Member] {

  override val startsOnApplicationBoot = true

  override def preStart() {
    super.preStart()
    context.system.oyunBus.subscribe(self, 'nbMembers, 'nbRounds)
  }

  override def postStop() {
    super.postStop()
    context.system.oyunBus.unsubscribe(self)
  }

  def receiveSpecific = {

    case PingVersion(uid, v) => Future {
      ping(uid)
      withMember(uid) { m =>
        history.since(v).fold {
          resync(m)
        }(_ foreach sendMessage(m))
      }
    }

    case Join(uid, user) =>
      val (enumerator, channel) = Concurrent.broadcast[JsValue]
      val member = Member(channel, user, uid)
      addMember(uid, member)
      sender ! Connected(enumerator, member)

    case ReloadMasas(html) => notifyAllAsync(makeMessage("masas", html))

    case AddHook(hook) =>
      notifyVersion("had", hook.render)

    case RemoveHook(hookId) => notifyVersion("hrm", hookId)

    case UpdateHook(hook) => notifyVersion("hup", hook.render)

    case JoinHook(uid, challengeId, side) =>
      withMember(uid)(notifyPlayerJoin(challengeId, side))

    case NbMembers(nb) => pong = pong + ("d" -> JsNumber(nb))
    case oyun.hub.actorApi.round.NbRounds(nb) =>
      pong = pong + ("r" -> JsNumber(nb))
  }

  private def notifyPlayerJoin(challengeId: String, side: okey.Side) = { member: Member =>
    notifyMember("redirect", Json.obj(
      "id" -> challengeId,
      "url" -> playerUrl(challengeId, side)
    ).noNull)(member)
  }
  private def playerUrl(challId: String, side: okey.Side) = s"/$challId/$side.name"
}
