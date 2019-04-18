package oyun.chat

import akka.actor._
import actorApi._

import okey.Side


private[chat] final class FrontActor(api: ChatApi) extends Actor {

  def receive = {

    case UserTalk(chatId, userId, text, public) =>
      api.userChat.write(chatId, userId, text, public)
    case PlayerTalk(chatId, side, text) =>
      api.playerChat.write(chatId, side, text)
    case SystemTalk(chatId, text) =>
      api.userChat.system(chatId, text)
  }

  // def publish(chatId: String, replyTo: ActorRef)(lineOption: Option[Line]) {
  //   lineOption foreach { line =>
  //     replyTo ! ChatLine(chatId, line)
  //   }
  // }

}
