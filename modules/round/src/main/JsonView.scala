package oyun.round

import play.api.libs.json._
import oyun.common.PimpedJson._

import oyun.game.{ Pov, Game, Player => GamePlayer }
import oyun.user.{ User, UserRepo }

import actorApi.SocketStatus

import okey.format.Forsyth

final class JsonView(
  chatApi: oyun.chat.ChatApi,
  userJsonView: oyun.user.JsonView,
  getSocketStatus: String => Fu[SocketStatus]) {

  import JsonView._

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
            "player" -> playerJson(socket, player, playerUser),
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

  private def playerJson(socket: SocketStatus, player: GamePlayer, playerUser: Option[User]) = Json.obj(
    "side" -> player.side.name,
    "version" -> socket.version,
    "ai" -> player.aiLevel,
    "user" -> playerUser.map { userJsonView(_) },
    "onGame" -> (player.isAi || socket.onGame(player.side)),
    "isGone" -> (!player.isAi || socket.isGone(player.side))
  )

  private def opponentJson(socket: SocketStatus, opponent: GamePlayer, opponentUser: Option[User]) = {
    Json.obj(
      "side" -> opponent.side.name,
      "ai" -> opponent.aiLevel,
      "user" -> opponentUser.map { userJsonView(_) },
      "onGame" -> (opponent.isAi || socket.onGame(opponent.side)),
      "isGone" -> (!opponent.isAi || socket.isGone(opponent.side))
    )
  }

  private def povJson(pov: Pov) = Json.obj(
    "id" -> pov.game.id,
    "fen" -> (Forsyth >> (pov.game.toOkey, pov.side)),
    "player" -> pov.game.turnSide.name,
    "scores" -> pov.game.endScores.map(sidesWriter(_)),
    "turns" -> pov.game.turns,
    "status" -> pov.game.status
  )

  private def getPlayerChat(game: Game, forUser: Option[User]): Fu[Option[oyun.chat.MixedChat]] =
    game.hasChat optionFu {
      chatApi.playerChat find game.id map (_ forUser forUser)
    }

  private def possibleMoves(pov: Pov) = (pov.game playableBy pov.player) option {
    pov.game.toOkey.situation.actions map { action =>
      action.key
    }
  }
}

object JsonView {
  implicit val statusWriter: OWrites[okey.Status] = OWrites { s =>
    Json.obj(
      "id" -> s.id,
      "name" -> s.name)
  }

  private def sidesWriter(sides: okey.Sides[okey.EndScoreSheet]) =
    Json.obj(
      "east" -> sides(okey.EastSide),
      "west" -> sides(okey.WestSide),
      "north" -> sides(okey.NorthSide),
      "south" -> sides(okey.SouthSide)
    )

  private implicit val endScoreSheetWriter: OWrites[okey.EndScoreSheet] = OWrites { s =>
    Json.obj(
      "hand" -> s.handSum,
      "total" -> s.total,
      "scores" -> JsObject(s.scores map {
        case (k, v) => k.id.toString -> (JsNumber(v.map(_.id) | 0))
      })
    )
  }
}
