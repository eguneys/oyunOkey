package oyun.game

import play.api.libs.json._

object AnonCookie {

  val name = "rk2"
  val maxAge = 604800

  def json(game: Game, side: okey.Side): Option[JsObject] =
    !game.player(side).userId.isDefined option Json.obj(
      "name" -> name,
      "maxAge" -> maxAge,
      "value" -> game.player(side).id
    )
  
}
