package oyun.round

import play.api.libs.json._
import oyun.common.PimpedJson._
import oyun.common.Maths.truncateAt

import oyun.game.{ Pov, Game, PerfPicker, Player => GamePlayer }
import oyun.user.{ User, UserRepo }

import actorApi.SocketStatus

import okey.format.Forsyth

final class JsonView(
  chatApi: oyun.chat.ChatApi,
  userJsonView: oyun.user.JsonView,
  getSocketStatus: String => Fu[SocketStatus]) {

  import JsonView._
  import oyun.game.GameJsonView._

  def playerJson(
    pov: Pov,
    playerUser: Option[User]): Fu[JsObject] = {
    val opponents = List(pov.opponentLeft, pov.opponentRight, pov.opponentUp)

    getSocketStatus(pov.game.id) zip
    (opponents.map(_.userId ?? UserRepo.byId).sequence) zip
      getPlayerChat(pov.game, playerUser) map {
        case ((socket, List(opponentLeftUser, opponentRightUser, opponentUpUser)), chat) =>
          import pov._
          Json.obj(
            "game" -> povJson(pov),
            "clock" -> game.clock.map(clockJson),
            "player" -> (playerJson(socket, player, playerUser) ++ Json.obj("spectator" -> false)),
            "opponentLeft" -> opponentJson(socket, opponentLeft, opponentLeftUser),
            "opponentRight" -> opponentJson(socket, opponentRight, opponentRightUser),
            "opponentUp" -> opponentJson(socket, opponentUp, opponentUpUser),
            "url" -> Json.obj(
              "socket" -> s"/$fullId/socket",
              "round" -> s"/$fullId"
            ),
            "chat" -> chat.map { c =>
              JsArray(c.lines map {
                case oyun.chat.UserLine(username, text, _) => Json.obj(
                  "u" -> username,
                  "t" -> text)
                case oyun.chat.PlayerLine(side, text) => Json.obj(
                  "s" -> side.name,
                  "t" -> text)
              })
            },
            "possibleMoves" -> possibleMoves(pov)
          ).noNull
      }
  }

  def watcherJson(
    pov: Pov,
    user: Option[User]): Fu[JsObject] = {
    val playerIds = List(pov.player, pov.opponentLeft, pov.opponentRight, pov.opponentUp) map (_.userId)

    getSocketStatus(pov.game.id) zip
    (UserRepo.pair(playerIds) map(_.toList)) zip
      getPlayerChat(pov.game, user) map {
        case ((socket, List(playerUser, opponentLeftUser, opponentRightUser, opponentUpUser)), chat) =>
          import pov._
          Json.obj(
            "game" -> (povJson(pov) ++ Json.obj(
              "fen" -> (Forsyth >>| (pov.game.toOkey, pov.side))
            )),
            "clock" -> game.clock.map(clockJson),
            "player" -> (playerJson(socket, player, playerUser) ++ Json.obj("spectator" -> true)),
            "opponentLeft" -> opponentJson(socket, opponentLeft, opponentLeftUser),
            "opponentRight" -> opponentJson(socket, opponentRight, opponentRightUser),
            "opponentUp" -> opponentJson(socket, opponentUp, opponentUpUser),
            "url" -> Json.obj(
              "socket" -> s"/$gameId/${side.name}/socket",
              "round" -> s"/$gameId/${side.name}"
            ),
            "chat" -> chat.map { c =>
              JsArray(c.lines map {
                case oyun.chat.UserLine(username, text, _) => Json.obj(
                  "u" -> username,
                  "t" -> text)
                case oyun.chat.PlayerLine(side, text) => Json.obj(
                  "s" -> side.name,
                  "t" -> text)
              })
            }
          ).noNull
      }
  }

  private def playerJson(socket: SocketStatus, player: GamePlayer, playerUser: Option[User]) = Json.obj(
    "side" -> player.side.name,
    "version" -> socket.version,
    "ai" -> player.aiLevel,
    "user" -> playerUser.map { userJsonView(_) },
    "rating" -> player.rating,
    "onGame" -> (player.isAi || socket.onGame(player.side)),
    "isGone" -> (!player.isAi || socket.isGone(player.side))
  )

  private def opponentJson(socket: SocketStatus, opponent: GamePlayer, opponentUser: Option[User]) = {
    Json.obj(
      "side" -> opponent.side.name,
      "ai" -> opponent.aiLevel,
      "rating" -> opponent.rating,
      "user" -> opponentUser.map { userJsonView(_) },
      "onGame" -> (opponent.isAi || socket.onGame(opponent.side)),
      "isGone" -> (!opponent.isAi || socket.isGone(opponent.side))
    )
  }

  private def povJson(pov: Pov) = Json.obj(
    "id" -> pov.game.id,
    "variant" -> pov.game.variant,
    "perf" -> PerfPicker.key(pov.game),
    "rated" -> pov.game.rated,
    "fen" -> (Forsyth >> (pov.game.toOkey, pov.side)),
    "player" -> pov.game.turnSide.name,
    "scores" -> pov.game.endScores.map(sidesWriter(_)),
    "oscores" -> pov.game.openStates.map(sidesWriter(_)),
    "turns" -> pov.game.turns,
    "status" -> pov.game.status,
    "masaId" -> pov.game.masaId,
    "roundAt" -> pov.game.roundAt,
    "createdAt" ->pov.game.createdAt).noNull

  private def getPlayerChat(game: Game, forUser: Option[User]): Fu[Option[oyun.chat.MixedChat]] =
    game.hasChat optionFu {
      chatApi.playerChat find game.id map (_ forUser forUser)
    }

  private def possibleMoves(pov: Pov) = (pov.game playableBy pov.player) option {
    pov.game.toOkey.situation.actions map { action =>
      action.key
    }
  }

  private def clockJson(clock: okey.Clock): JsObject =
    clockWriter.writes(clock)
}

object JsonView {

  implicit val variantWriter: OWrites[okey.variant.Variant] = OWrites { v =>
    Json.obj(
      "key" -> v.key,
      "name" -> v.name,
      "short" -> v.shortName,
      "title" -> v.title)
  }

  implicit val clockWriter: OWrites[okey.Clock] = OWrites { c =>
    Json.obj(
      "running" -> c.isRunning,
      "initial" -> c.limit,
      "sides" -> JsObject(
        okey.Side.all.map(s => s.name -> JsNumber(truncateAt(c.remainingTime(s), 2)))),
      "emerg" -> c.emergTime
    )
  }
}
