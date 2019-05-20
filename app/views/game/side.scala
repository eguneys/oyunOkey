package views.html
package game

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import controllers.routes

object side {

  private val separator = " • "

  def apply(pov: oyun.game.Pov,
    m: Option[oyun.masa.Masa])(implicit ctx: Context): Option[Frag] = none

}
