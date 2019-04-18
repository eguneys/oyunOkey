package oyun.lobby

import akka.actor._

import play.api.libs.json._

import oyun.common.PimpedJson._

import actorApi._
import oyun.common.Tellable
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.socket.{ Socket, Handler }
import oyun.user.User

private[lobby] final class SocketHandler(
  hub: oyun.hub.Env,
  lobby: LobbyTrouper,
  socket: LobbySocket) extends Tellable.PartialReceive with Tellable.HashCode {

  private var pong = Socket.initialPong

  protected val receive: Tellable.Receive = {
    case oyun.socket.actorApi.NbMembers(nb) => pong = pong + ("d" -> JsNumber(nb))
    case oyun.hub.actorApi.round.NbRounds(nb) => pong = pong + ("r" -> JsNumber(nb))
  }

  private def controller(
    socket: LobbySocket,
    member: Member): Handler.Controller = {
    case ("join", o) => {
      o str "d" foreach { id =>
        lobby ! BiteHook(id, member.uid, member.user)
      }
    }
    case ("cancel", o) => lobby ! CancelHook(member.uid)
  }

  def apply(uid: Socket.Uid, user: Option[User]): Fu[JsSocketHandler] = {
    funit flatMap { _ =>
      socket.ask[Connected](Join(uid, user, _)) map {
        case Connected(enum, member) => Handler.iteratee(
          hub,
          controller(socket, member),
          member,
          socket,
          uid,
          onPing = (_, _, _) => {
            socket setAlive uid
            member push pong
          }
        ) -> enum
      }
    }
  }

}
