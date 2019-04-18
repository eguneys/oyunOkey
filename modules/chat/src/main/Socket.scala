package oyun.chat

import akka.actor._
import play.api.libs.json._

import oyun.socket.{ Handler, SocketMember }
import oyun.user.User

object Socket {

  def in(
    chatId: Chat.Id,
    member: SocketMember,
    chat: ActorSelection): Handler.Controller = {

    case ("talk", o) =>
      for {
      text <- o str "d"
      userId <- member.userId
    } chat ! actorApi.UserTalk(chatId, userId, text)
    
  }

}
