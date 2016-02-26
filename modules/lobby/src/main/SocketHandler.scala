package oyun.lobby

import akka.actor._

import oyun.common.PimpedJson._

import actorApi._
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.socket.Handler
import oyun.user.User

private[lobby] final class SocketHandler(socket: ActorRef) {

  private def controller(
    socket: ActorRef,
    uid: String,
    member: Member): Handler.Controller = {
    case ("p", o) => o int "v" foreach { v => socket ! PingVersion(uid, v) }
  }

  def apply(uid: String, user: Option[User]): Fu[JsSocketHandler] = {
    val join = Join(uid = uid, user = user)
    Handler(socket, uid, join) {
      case Connected(enum, member) =>
        (controller(socket, uid, member), enum, member)
    }
  }
}
