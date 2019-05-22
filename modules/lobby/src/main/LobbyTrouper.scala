package oyun.lobby

import akka.actor._
import akka.pattern.{ ask, pipe }

import actorApi._
import oyun.hub.actorApi.map.{ Tell }
import oyun.hub.actorApi.{ GetUids, SocketUids }
import oyun.hub.Trouper
import oyun.socket.actorApi.Broom
import makeTimeout.short
import org.joda.time.DateTime

private[lobby] final class LobbyTrouper(
  system: akka.actor.ActorSystem,
  socket: LobbySocket) extends Trouper {

  val process: Trouper.Receive = {
    // case HooksFor(userOption) =>
    //   val replyTo = sender
    //   val lobbyUser = userOption map { LobbyUser.make(_) }
    //   replyTo ! HookRepo.list

    case msg@AddHook(hook) => {
      HookRepo byUid hook.uid foreach remove
      findCompatible(hook) foreach {
        case Some(h) => this ! BiteHook(h.id, hook.uid, hook.user)
        case None => this ! SaveHook(msg)
      }
    }
    case SaveHook(msg) =>
      HookRepo save msg.hook
      socket ! msg
    case CancelHook(uid) =>
      HookRepo byUid uid foreach remove

    case BiteHook(hookId, uid, user) =>

    case msg:JoinHook =>
      socket ! msg

    // case Broom =>
    //   (socket ? GetUids mapTo manifest[SocketUids]).effectFold(
    //     err => play.api.Logger("lobby").warn(s"broom cannot get uids from socket: $err"),
    //     socketUids => {
    //       val createdBefore = DateTime.now minusSeconds 5
    //       val hooks = {
    //         (HookRepo notInUids socketUids.uids).filter {
    //           _.createdAt isBefore createdBefore
    //         }
    //       }.toSet

    //       if (hooks.nonEmpty) {
    //         this ! RemoveHooks(hooks)
    //       }
    //     })

    case RemoveHooks(hooks) => hooks foreach remove
  }

  private def findCompatible(hook: Hook): Fu[Option[Hook]] =
    fuccess(None)

  private def remove(hook: Hook) = {
    HookRepo remove hook
    socket ! RemoveHook(hook.id)
  }

  private def update(hook: Hook) = {
    HookRepo update hook
    socket ! UpdateHook(hook)
  }
}

private object LobbyTrouper {

  def start(system: akka.actor.ActorSystem)(makeTrouper: () => LobbyTrouper) = {

    val trouper = makeTrouper()

    system.oyunBus.subscribe(trouper, 'lobbyTrouper)

    trouper
  }

}
