package oyun.lobby

import akka.actor._
import akka.pattern.{ ask, pipe }

import actorApi._
import oyun.hub.actorApi.{ GetUids, SocketUids }
import oyun.socket.actorApi.Broom
import makeTimeout.short
import org.joda.time.DateTime

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
    case CancelHook(uid) =>
      HookRepo byUid uid foreach remove

    case BiteHook(hookId, uid, user) => {
      HookRepo byId hookId foreach { hook =>
        HookRepo byUid uid foreach remove
        Biter(hook, uid, user) pipeTo self
      }
    }

    case msg:JoinHook =>
      socket ! msg

    case Broom =>
      (socket ? GetUids mapTo manifest[SocketUids]).effectFold(
        err => play.api.Logger("lobby").warn(s"broom cannot get uids from socket: $err"),
        socketUids => {
          val createdBefore = DateTime.now minusSeconds 5
          val hooks = {
            (HookRepo notInUids socketUids.uids).filter {
              _.createdAt isBefore createdBefore
            }
          }.toSet

          if (hooks.nonEmpty) {
            self ! RemoveHooks(hooks)
          }
        })

    case RemoveHooks(hooks) => hooks foreach remove
  }

  private def findCompatible(hook: Hook): Fu[Option[Hook]] =
    fuccess(None)

  private def remove(hook: Hook) = {
    HookRepo remove hook
    socket ! RemoveHook(hook.id)
  }

}
