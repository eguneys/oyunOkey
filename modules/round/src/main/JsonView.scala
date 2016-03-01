package oyun.round

import play.api.libs.json._
import oyun.common.PimpedJson._

import oyun.game.{ Pov, Game, Player }
import oyun.user.{ User }

import actorApi.SocketStatus

import okey.format.Forsyth

final class JsonView(
  getSocketStatus: String => Fu[SocketStatus]) {

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
          "opponentLeft" -> (opponentLeft map opponentJson),
          "opponentRight" -> (opponentRight map opponentJson),
          "opponentUp" -> (opponentUp map opponentJson),
          "url" -> Json.obj(
            "socket" -> s"/$fullId/socket",
            "round" -> s"/$fullId"
          )
        ).noNull
    }

  private def opponentJson(opponent: Player) = Json.obj(
    "side" -> opponent.side.name
  )

  private def povJson(pov: Pov) = Json.obj(
    "id" -> pov.game.id,
    "fen" -> (Forsyth >> (pov.game.toOkey, pov.side)),
    "player" -> pov.game.turnSide.name
  )
}
