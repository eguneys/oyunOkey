package views.html

import play.api.libs.json.Json

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._
import oyun.i18n.I18nKeys

import controllers.routes

object chat {

  val frag = st.section(cls := "mchat")(
    div(cls := "mchat__tabs")(
      div(cls := "mchat__tab")(nbsp)
    ),
    div(cls := "mchat__content")
  )
  
}
