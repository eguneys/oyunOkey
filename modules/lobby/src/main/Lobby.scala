package oyun.lobby

import akka.actor._
import akka.pattern.{ ask, pipe }

import actorApi._

private[lobby] final class Lobby(
  socket: ActorRef) extends Actor {

  def receive = {
    case HooksFor(userOption) =>
      val replyTo = sender
      val lobbyUser = userOption map { LobbyUser.make(_) }
      replyTo ! HookRepo.list

    case msg@AddHook(hook) => {
      HookRepo byUid hook.uid foreach remove
      findCompatible(hook) foreach {
        case Some(h) => self ! BiteHook(h.id, hook.uid, hook.user)
        case None => self ! SaveHook(msg)
      }
    }
    case SaveHook(msg) =>
      HookRepo save msg.hook
      socket ! msg
  }

  private def findCompatible(hook: Hook): Fu[Option[Hook]] =
    fuccess(None)

  private def remove(hook: Hook) = {
    HookRepo remove hook
    socket ! RemoveHook(hook.id)
  }

}
