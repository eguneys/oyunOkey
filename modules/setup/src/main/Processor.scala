package oyun.setup

import akka.actor.ActorSelection
import akka.pattern.ask

import oyun.lobby.actorApi.{ AddHook }
import oyun.user.{ UserContext }

private[setup] final class Processor(
  lobby: ActorSelection) {

  def hook(
    config: HookConfig,
    uid: String,
    sid: Option[String])(implicit ctx: UserContext): Fu[String] = {

    val hook = config.hook(uid, ctx.me, sid)
    fuccess {
      lobby ! AddHook(hook)
      hook.id
    }
  }
}
