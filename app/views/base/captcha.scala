package views.html.base

import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import oyun.api.Context

import controllers.routes

object captcha {

  private val dataCheckUrl = attr("data-check-url")

  def apply(form: oyun.common.Form.FormLike, captcha: oyun.common.Captcha)(implicit ctx: Context) = frag(
    form3.hidden(form("id"), captcha.id.some),
    div(
      cls := List(
        "captcha-form-group" -> true,
        "is-invalid" -> oyun.common.Captcha.isFailed(form)
      ),
      dataCheckUrl := routes.Main.captchaCheck(captcha.id)
    )(
      div(cls := "captcha-explanation")(
        br,br
      )
    )
  )
}
