package oyun.api

import akka.actor.ActorRef
import akka.pattern.ask
import play.api.libs.json.{ Json, JsObject, JsArray }

import oyun.lobby.actorApi.HooksFor
import oyun.lobby.{ Hook, HookRepo }

final class LobbyApi(
  lobby: ActorRef,
  lobbyVersion: () => Int) {

  import makeTimeout.large

  def apply(implicit ctx: Context): Fu[JsObject] =
    (lobby ? HooksFor(ctx.me)).mapTo[List[Hook]] map {
      case hooks => Json.obj(
        "me" -> ctx.me.map { u =>
          Json.obj("username"-> u.username)
        },
        "version" -> lobbyVersion(),
        "hooks" -> JsArray(hooks map (_.render)))
    }
}
