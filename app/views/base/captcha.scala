package views.html.base

import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import oyun.api.Context

import controllers.routes

object captcha {

  private val dataCheckUrl = attr("data-check-url")

  def apply(form: oyun.common.Form.FormLike)(implicit ctx: Context) = frag(
    div(
      cls := List(
        "captcha-form-group" -> true,
        "is-invalid" -> oyun.common.Captcha.isFailed(form)
      )
    )(
      div(cls := "captcha-explanation")(
        br,br
      )
    )
  )
}
