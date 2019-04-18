package oyun.chat
package actorApi

import akka.actor.ActorRef

import okey.Side

case class UserTalk(chatId: Chat.Id, userId: String, text: String, public: Boolean = true)
case class PlayerTalk(chatId: Chat.Id, side: Side, text: String)
case class SystemTalk(chatId: Chat.Id, text: String)
case class ChatLine(chatId: Chat.Id, line: Line)


