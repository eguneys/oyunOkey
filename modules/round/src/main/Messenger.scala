package oyun.round

import akka.actor._

import actorApi._
import oyun.game.Game
import oyun.chat.Chat
import oyun.chat.actorApi._

final class Messenger(
  chat: ActorSelection) {



  def watcher(gameId: String, member: Member, text: String) =
    member.userId foreach { userId =>
      chat ! UserTalk(Chat.Id(watcherId(gameId)), userId, text)
    }

  def owner(gameId: String, member: Member, text: String) =
    (member.userId match {
      case Some(userId) => UserTalk(Chat.Id(gameId), userId, text, public = false).some
      case None => PlayerTalk(Chat.Id(gameId), member.side, text).some
    }) foreach chat.!

  private def watcherId(gameId: Game.ID) = s"$gameId/w"
}
