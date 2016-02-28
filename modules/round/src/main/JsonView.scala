package oyun.round

import play.api.libs.json._
import oyun.common.PimpedJson._

import oyun.game.{ Pov }
import oyun.user.{ User }

import actorApi.SocketStatus

final class JsonView(
  getSocketStatus: String => Fu[SocketStatus]) {

  def playerJson(
    pov: Pov,
    playerUser: Option[User]): Fu[JsObject] =
    getSocketStatus(pov.game.id) map {
      case (socket) =>
        import pov._
        Json.obj(
          "player" -> Json.obj(
            // "id" -> playerId,
            // "side" -> player.side,
            "version" -> socket.version
          ),
          "url" -> Json.obj(
            "socket" -> s"/$fullId/socket",
            "round" -> s"/$fullId"
          )
        ).noNull
    }
}
