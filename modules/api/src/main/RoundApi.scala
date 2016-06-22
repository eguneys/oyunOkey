package oyun.api

import play.api.libs.json._
import oyun.common.PimpedJson._

import oyun.game.{ Game, Pov }

import oyun.round.{ JsonView }
import oyun.masa.{ Masa }

private[api] final class RoundApi(
  jsonView: JsonView,
  getMasa: Game => Fu[Option[Masa]]) {

  def player(pov: Pov)(implicit ctx: Context): Fu[JsObject] =
    jsonView.playerJson(pov, ctx.me) zip
      getMasa(pov.game) map {
        case ((json, masaOption)) => (
          withMasa(pov, masaOption)_ compose
            withSteps(pov)_
        )(json)
      }

  def watcher(pov: Pov)(implicit ctx: Context): Fu[JsObject] =
    jsonView.watcherJson(pov, ctx.me) zip
      getMasa(pov.game) map {
        case ((json, masaOption)) => (
          withMasa(pov, masaOption)_ compose
            withSteps(pov)_
        )(json)
      }

  private def withSteps(pov: Pov)(obj: JsObject) =
    obj + ("steps" -> oyun.round.StepBuilder(
      id = pov.game.id,
      ply = pov.game.opensLastMove.turn,
      pgnMoves = pov.game.opensLastMove.lastMoves.map(_.uci),
      side = okey.Side(pov.game.opensLastMove.turn),
      variant = pov.game.variant))

  private def withMasa(pov: Pov, masaOption: Option[Masa])(json: JsObject) =
    masaOption.fold(json) { data =>
      val masa = data
      json + ("masa" -> Json.obj(
        "id" -> masa.id,
        "name" -> masa.fullName,
        "running" -> masa.isStarted
      ).noNull)
    }

}
