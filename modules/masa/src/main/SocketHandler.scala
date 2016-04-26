package oyun.masa

import akka.actor._
import akka.pattern.ask

import actorApi._
import oyun.common.PimpedJson._
import oyun.socket.Handler
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.hub.actorApi.map._
import oyun.user.User
import makeTimeout.short


private[masa] final class SocketHandler(
  hub: oyun.hub.Env,
  socketHub: ActorRef) {
  def join(
    masaId: String,
    uid: String,
    user: Option[User],
    player: Option[Player]): Fu[Option[JsSocketHandler]] =
    MasaRepo.exists(masaId) flatMap {
      _ ?? {
        for {
          socket <- socketHub ? Get(masaId) mapTo manifest[ActorRef]
          join = Join(uid = uid, user = user, player = player)
          handler <- Handler(socket, uid, join) {
            case Connected(enum, member) =>
              (controller(socket, masaId, uid, member), enum, member)
          }
        } yield handler.some
      }
    }

  private def controller(
    socket: ActorRef,
    masaId: String,
    uid: String,
    member: Member): Handler.Controller = {
    case ("p", o) => o int "v" foreach { v => socket ! PingVersion(uid, v) }
  }
}
