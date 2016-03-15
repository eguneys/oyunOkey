package oyun.round

import play.api.libs.json._
import oyun.common.PimpedJson._

import oyun.game.{ Pov, Game, Player => GamePlayer }
import oyun.user.{ User }

import actorApi.SocketStatus

import okey.format.Forsyth

final class JsonView(
  getSocketStatus: String => Fu[SocketStatus]) {

  import JsonView._

  def playerJson(
    pov: Pov,
    playerUser: Option[User]): Fu[JsObject] =
    getSocketStatus(pov.game.id) map {
      case (socket) =>
        import pov._
        Json.obj(
          "game" -> povJson(pov),
          "player" -> Json.obj(
            "side" -> side.name,
            "version" -> socket.version
          ),
          "opponentLeft" -> opponentJson(socket, opponentLeft),
          "opponentRight" -> opponentJson(socket, opponentRight),
          "opponentUp" -> opponentJson(socket, opponentUp),
          "url" -> Json.obj(
            "socket" -> s"/$fullId/socket",
            "round" -> s"/$fullId"
          ),
          "possibleMoves" -> possibleMoves(pov)
        ).noNull
    }

  private def opponentJson(socket: SocketStatus, opponent: GamePlayer) = Json.obj(
    "side" -> opponent.side.name,
    "onGame" -> socket.onGame(opponent.side),
    "isGone" -> socket.isGone(opponent.side)
  )

  private def povJson(pov: Pov) = Json.obj(
    "id" -> pov.game.id,
    "fen" -> (Forsyth >> (pov.game.toOkey, pov.side)),
    "player" -> pov.game.turnSide.name,
    "turns" -> pov.game.turns,
    "status" -> pov.game.status
  )

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
}
