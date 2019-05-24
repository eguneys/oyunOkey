package views.html
package round

import play.api.libs.json.Json

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._
import oyun.common.String.html.safeJsonValue
import okey.variant.{ Variant }
import oyun.game.{ Game, Pov, Player }

import controllers.routes

object bits {

  def layout(
    variant: Variant,
    title: String,
    moreJs: Frag = emptyFrag,
    openGraph: Option[oyun.app.ui.OpenGraph] = None,
    moreCss: Frag = emptyFrag,
    okeyground: Boolean = true,
    playing: Boolean = false)(body: Frag)(implicit ctx: Context) =
    views.html.base.layout(
      title = title,
      openGraph = openGraph,
      moreJs = moreJs,
      moreCss = frag(
        cssTag("round"),
        moreCss
      ),
      okeyground = okeyground,
      playing = playing,
      deferJs = true
    )(body)

  def underchat(game: Game)(implicit ctx: Context) = frag(
    div(cls := "chat__members none")(
      span(cls := "number")(nbsp),
      " ",
      trans.spectators.txt().replace(":", ""),
      " ",
      span(cls := "list")
    )
  )

  private[round] def side(
    pov: Pov,
    data: play.api.libs.json.JsObject,
    m: Option[oyun.masa.MasaMiniView])(implicit ctx: Context) = views.html.game.side(
    pov,
      m.map(_.masa)
  )

  def roundAppPreload(pov: Pov, controls: Boolean)(implicit ctx: Context) =
    div(cls := "round__app")(
      div(cls := "round__app__board main-board")(okeyground(pov)),
      div(cls := "round__app__table"),
      div(cls := "rmoves")(div(cls := "moves")),
      controls option div(cls := "rcontrols")(i(cls := "ddloader"))
    )

}
