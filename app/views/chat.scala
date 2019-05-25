package views.html

import play.api.libs.json._

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._
import oyun.i18n.I18nKeys

import controllers.routes

object chat {

  def json(name: String)(implicit ctx: Context) = Json.obj(
    "data" -> Json.obj(
      "name" -> name,
      "lines" -> JsArray()
    ),
    "i18n" -> i18n()
  )

  def i18n()(implicit ctx: Context) = i18nJsObject(List(
    I18nKeys.talkInChat
  ))

  val frag = st.section(cls := "mchat")(
    div(cls := "mchat__tabs")(
      div(cls := "mchat__tab")(nbsp)
    ),
    div(cls := "mchat__content")
  )
  
}
