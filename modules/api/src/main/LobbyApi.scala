package oyun.api

import akka.actor.ActorRef
import akka.pattern.ask
import play.api.libs.json.{ Json, JsObject, JsArray }

import oyun.common.PimpedJson._
import oyun.common.LightUser
import oyun.game.{ GameRepo, Pov }
import oyun.lobby.SeekApi
import oyun.lobby.actorApi.HooksFor
import oyun.lobby.{ Hook, HookRepo }

final class LobbyApi(
  lobbyVersion: () => Int,
  lightUser: String => Option[LightUser],
  seekApi: SeekApi) {

  import makeTimeout.large

  def apply(implicit ctx: Context): Fu[(JsObject, List[Pov])] =
    ctx.me.fold(seekApi.forAnon)(seekApi.forUser) zip
    ctx.me ?? GameRepo.urgentGames flatMap {
      case (seeks, povs) =>
        val displayedPovs = povs take 9

        funit inject {
          Json.obj(
            "me" -> ctx.me.map { u =>
              Json.obj("username" -> u.username)
            },
            "seeks" -> JsArray(seeks map (_.render)),
            "nowPlaying" -> JsArray(displayedPovs map nowPlaying),
            "nbNowPlaying" -> povs.size
          ) -> displayedPovs
        }
    }
    // (lobby ? HooksFor(ctx.me)).mapTo[List[Hook]] zip
    //   (ctx.me ?? GameRepo.urgentGames) map {
    //     case (hooks, povs) => Json.obj(
    //       "me" -> ctx.me.map { u =>
    //         Json.obj("username"-> u.username)
    //       },
    //       "version" -> lobbyVersion(),
    //       "hooks" -> JsArray(hooks map (_.render)),
    //       "nowPlaying" -> JsArray(povs take 9 map nowPlaying),
    //       "nbNowPlaying" -> povs.size)
    //   }

  def nowPlaying(pov: Pov) = Json.obj(
    "fullId" -> pov.fullId,
    "gameId" -> pov.gameId,
    "fen" -> (okey.format.Forsyth >> (pov.game.toOkey, pov.side)),
    "side" -> pov.side.name,
    "variant" -> Json.obj("key" -> pov.game.variant.key, "name" -> pov.game.variant.name),
    "perf" -> oyun.game.PerfPicker.key(pov.game),
    "rated" -> pov.game.rated,
    "isMyTurn" -> pov.isMyTurn,
    "opponent" -> Json.obj(
      "id" -> pov.opponentUp.userId,
      "username" -> oyun.game.Namer.playerString(pov.opponentUp, withRating = false)(lightUser),
      "ai" -> pov.opponentUp.aiLevel).noNull
  ).noNull
}
