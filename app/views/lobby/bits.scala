package views.html.lobby

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import controllers.routes

object bits {

  val lobbyApp = div(cls := "lobby__app")(
    div(cls := "tabs-horiz")(span(nbsp)),
    div(cls := "lobby__app__content")
  )

}
