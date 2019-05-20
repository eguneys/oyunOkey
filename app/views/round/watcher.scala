package views.html
package round

import play.api.libs.json.Json

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._
import oyun.common.String.html.safeJsonValue
import oyun.game.Pov

import controllers.routes

object watcher {

  def apply(
    pov: Pov,
    data: play.api.libs.json.JsObject,
    m: Option[oyun.masa.MasaMiniView])(implicit ctx: Context) = {
    
    ""

  }

}
