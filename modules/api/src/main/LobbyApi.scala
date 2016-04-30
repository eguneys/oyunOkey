package oyun.api

import akka.actor.ActorRef
import akka.pattern.ask
import play.api.libs.json.{ Json, JsObject, JsArray }

import oyun.game.{ GameRepo, Pov }
import oyun.lobby.actorApi.HooksFor
import oyun.lobby.{ Hook, HookRepo }

final class LobbyApi(
  lobby: ActorRef,
  lobbyVersion: () => Int) {

  import makeTimeout.large

  def apply(implicit ctx: Context): Fu[JsObject] =
    (lobby ? HooksFor(ctx.me)).mapTo[List[Hook]] zip
      (ctx.me ?? GameRepo.urgentGames) map {
        case (hooks, povs) => Json.obj(
          "me" -> ctx.me.map { u =>
            Json.obj("username"-> u.username)
          },
          "version" -> lobbyVersion(),
          "hooks" -> JsArray(hooks map (_.render)),
          "nowPlaying" -> JsArray(povs take 9 map nowPlaying),
          "nbNowPlaying" -> povs.size)
      }

  def nowPlaying(pov: Pov) = Json.obj(
    "fullId" -> pov.fullId,
    "gameId" -> pov.gameId,
    "variant" -> Json.obj("key" -> pov.game.variant.key, "name" -> pov.game.variant.name)
  )
}
