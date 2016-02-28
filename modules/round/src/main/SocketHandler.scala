package oyun.round

import akka.actor._
import akka.pattern.{ ask }
import play.api.libs.json.{ JsObject, Json }
import oyun.common.PimpedJson._

import actorApi._
import oyun.hub.actorApi.map._
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.socket.Handler
import oyun.user.User
import oyun.game.{ Game, Pov, GameRepo }
import makeTimeout.short

private[round] final class SocketHandler(
  socketHub: ActorRef
) {

  private def controller(
    gameId: String,
    socket: ActorRef,
    uid: String,
    member: Member): Handler.Controller = {
    case ("p", o) => o int "v" foreach { v => socket ! PingVersion(uid, v) }
  }

  def player(
    pov: Pov,
    uid: String,
    user: Option[User]): Fu[JsSocketHandler] =
    join(pov, Some(pov.playerId), uid, user)


  private def join(
    pov: Pov,
    playerId: Option[String],
    uid: String,
    user: Option[User]): Fu[JsSocketHandler] = {
    val join = Join(
      uid = uid,
      user = user,
      side = pov.side,
      playerId = playerId)
    socketHub ? Get(pov.gameId) mapTo manifest[ActorRef] flatMap { socket =>
      Handler(socket, uid, join) {
        case Connected(enum, member) =>
          (controller(pov.gameId, socket, uid, member), enum, member)
      }
    }
  }
}
