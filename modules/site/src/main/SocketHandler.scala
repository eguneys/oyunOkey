package oyun.site

import akka.actor._
import play.api.libs.json._

import actorApi._
import oyun.socket._

private[site] final class SocketHandler(
  socket: Socket,
  hub: oyun.hub.Env) {

  def human(uid: Socket.Uid,
    userId: Option[String],
    flag: Option[String]): Fu[JsSocketHandler] = {

    socket.ask[Connected](Join(uid, userId, flag, _)) map {
      case Connected(enum, member) => Handler.iteratee(
        hub,
        controller = PartialFunction.empty,
        member,
        socket,
        uid
      ) -> enum
    }
    
  }

  // def apply(
  //   uid: String,
  //   userId: Option[String],
  //   flag: Option[String]): Fu[JsSocketHandler] = {

  //   socket.ask[Connected](Join(uid, userId, flag, _)) map {
  //     case Connected(enum, member) => Handler.iteratee(
  //       hub,
  //       controller = PartialFunction.empty,
  //       member,
  //       socket,
  //       uid
  //     ) -> enum
  //   }
  //   // Handler(socket, uid, Join(uid, userId, flag)) {
  //   //   case Connected(enum, member) => (Handler.emptyController, enum, member)
  //   // }
  // }

}
