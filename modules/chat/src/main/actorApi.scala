package oyun.chat
package actorApi

import akka.actor.ActorRef

import okey.Side

case class UserTalk(chatId: String, userId: String, text: String, replyTo: ActorRef, public: Boolean = true)
case class PlayerTalk(chatId: String, side: Side, text: String, replyTo: ActorRef)
case class SystemTalk(chatId: String, text: String, replyTo: ActorRef)
case class ChatLine(chatId: String, line: Line)

