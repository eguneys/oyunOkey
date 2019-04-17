package oyun.lobby

import akka.actor._

import oyun.common.PimpedJson._

import actorApi._
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.socket.Handler
import oyun.user.User

private[lobby] final class SocketHandler(
  lobby: LobbyTrouper,
  socket: LobbySocket) {

  private def controller(
    socket: ActorRef,
    uid: String,
    member: Member): Handler.Controller = {
    case ("p", o) => o int "v" foreach { v => socket ! PingVersion(uid, v) }
    case ("join", o) => {
      o str "d" foreach { id =>
        lobby ! BiteHook(id, uid, member.user)
      }
    }
    case ("cancel", o) => lobby ! CancelHook(uid)
  }

  def apply(uid: String, user: Option[User]): Fu[JsSocketHandler] = {
    // val join = Join(uid = uid, user = user)
    // Handler(socket, uid, join) {
    //   case Connected(enum, member) =>
    //     (controller(socket, uid, member), enum, member)
    // user flatMap {
    //   socket.ask[Connected](Join(uid, user)) map {
    //     case Connected(enum, member) => Handler.iteratee(
    //       hub,
    //       controller(socket, member),
    //       member,
    //       socket,
    //       uid,
    //       onPing = (_, _, _, _) => {
    //         // socket setAlive uid
    //         // member push pong
    //       }
    //     ) -> enum
    //   }
    // }
    ???
  }

}
