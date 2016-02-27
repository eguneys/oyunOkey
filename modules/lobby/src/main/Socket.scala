package oyun.lobby

import akka.pattern.ask
import play.api.libs.json._
import play.api.libs.iteratee._

import actorApi._
import oyun.common.PimpedJson._
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

    case JoinHook(uid, hook, game, side) =>
      withMember(hook.uid)(notifyPlayerStart(game, okey.EastSide))
      withMember(uid)(notifyPlayerStart(game, side))
  }

  private def notifyPlayerStart(game: oyun.game.Game, side: okey.Side) = { member: Member =>
    game fullIdOf side foreach { fullId =>
      notifyMember("redirect", Json.obj(
        "id" -> (fullId),
        "url" -> playerUrl(fullId)
      ).noNull)(member)
    }
  }
  private def playerUrl(fullId: String) = s"/$fullId"
}
