package oyun.chat

import akka.actor._
import actorApi._

import okey.Side


private[chat] final class FrontActor(api: ChatApi) extends Actor {

  def receive = {

    case UserTalk(chatId, userId, text, replyTo, public) =>
      api.userChat.write(chatId, userId, text, public) foreach publish(chatId, replyTo)
    case PlayerTalk(chatId, side, text, replyTo) =>
      api.playerChat.write(chatId, side, text) foreach publish(chatId, replyTo)
    case SystemTalk(chatId, text, replyTo) =>
      api.userChat.system(chatId, text) foreach publish(chatId, replyTo)
  }

  def publish(chatId: String, replyTo: ActorRef)(lineOption: Option[Line]) {
    lineOption foreach { line =>
      replyTo ! ChatLine(chatId, line)
    }
  }

}
