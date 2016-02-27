package oyun.api

import play.api.libs.json._

import oyun.game.{ Pov }

import oyun.round.{ JsonView }

private[api] final class RoundApi(
  jsonView: JsonView) {

  def player(pov: Pov)(implicit ctx: Context): Fu[JsObject] = fufail("klasdjf")

}
