package oyun.site

import akka.actor._
import play.api.libs.json._

import actorApi._
import oyun.socket._

private[site] final class SocketHandler(
  socket: ActorRef,
  hub: oyun.hub.Env) {

  def apply(
    uid: String,
    userId: Option[String],
    flag: Option[String]): Fu[JsSocketHandler] = {

    Handler(socket, uid, Join(uid, userId, flag)) {
      case Connected(enum, member) => (Handler.emptyController, enum, member)
    }
  }

}
