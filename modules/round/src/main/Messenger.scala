package oyun.round

import akka.actor._

import actorApi._
import oyun.chat.actorApi._

final class Messenger(
  socketHub: ActorRef,
  chat: ActorSelection) {



  def watcher(gameId: String, member: Member, text: String, socket: ActorRef) =
    member.userId foreach { userId =>
      chat ! UserTalk(gameId + "/w", userId, text, socket)
    }

  def owner(gameId: String, member: Member, text: String, socket: ActorRef) =
    chat ! (member.userId match {
      case Some(userId) => UserTalk(gameId, userId, text, socket, public = false)
      case None => PlayerTalk(gameId, member.side, text, socket)
    })
}
