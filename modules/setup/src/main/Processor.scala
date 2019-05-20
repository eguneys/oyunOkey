package oyun.setup

import akka.actor.ActorSelection
import akka.pattern.ask

import oyun.lobby.actorApi.{ AddHook }
import oyun.user.{ UserContext }
import oyun.masa.{ Masa, PlayerRef }

private[setup] final class Processor(
  lobby: ActorSelection) {

  def ai(config: AiConfig, playerRef: PlayerRef)(implicit ctx: UserContext): Fu[Masa] = ???

  def hook(
    config: HookConfig,
    uid: oyun.socket.Socket.Uid,
    sid: Option[String])(implicit ctx: UserContext): Fu[String] = {

    val hook = config.hook(uid, ctx.me, sid)
    fuccess {
      lobby ! AddHook(hook)
      hook.id
    }
  }
}
