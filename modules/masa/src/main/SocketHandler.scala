package oyun.masa

import akka.actor._
import akka.pattern.ask

import actorApi._
import oyun.common.PimpedJson._
import oyun.chat.Chat
import oyun.socket.Handler
import oyun.socket.Socket
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.hub.actorApi.map._
import oyun.user.User
import makeTimeout.short


private[masa] final class SocketHandler(
  hub: oyun.hub.Env,
  socketMap: SocketMap,
  chat: ActorSelection) {

  def join(
    masaId: String,
    uid: Socket.Uid,
    user: Option[User],
    player: Option[Player]): Fu[Option[JsSocketHandler]] =
      MasaRepo.exists(masaId) flatMap {
        _ ?? {
          val socket = socketMap getOrMake masaId
          socket.ask[Connected](Join(uid, user, player, _)) map {
            case Connected(enum, member) => Handler.iteratee(
              hub,
              oyun.chat.Socket.in(
                chatId = Chat.Id(masaId),
                member = member,
                chat = chat
              ),
              member,
              socket,
              uid
            ) -> enum
          } map some
        }
      }
    // MasaRepo.exists(masaId) flatMap {
    //   _ ?? {
    //     for {
    //       socket <- socketHub ? Get(masaId) mapTo manifest[ActorRef]
    //       join = Join(uid = uid, user = user, player = player)
    //       handler <- Handler(socket, uid, join) {
    //         case Connected(enum, member) =>
    //           (controller(socket, masaId, uid, member), enum, member)
    //       }
    //     } yield handler.some
    //   }
    // }

  // private def controller(
  //   socket: ActorRef,
  //   masaId: String,
  //   uid: String,
  //   member: Member): Handler.Controller = {
  //   case ("p", o) => o int "v" foreach { v => socket ! PingVersion(uid, v) }
  //   case ("talk", o) => o str "d" foreach { text =>
  //     member.userId foreach { userId =>
  //       chat ! oyun.chat.actorApi.UserTalk(masaId, userId, text, socket)
  //     }
  //   }
  // }
}
