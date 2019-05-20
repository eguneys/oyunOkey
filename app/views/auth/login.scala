package views.html
package auth

import play.api.data.Form

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import controllers.routes

object login {

  def apply(form: Form[_], referrer: Option[String])(implicit ctx: Context) = views.html.base.layout(
    title = trans.signIn.txt(),
    moreJs = jsTag("login.js"),
    moreCss = cssTag("auth")
  ) {
    main(cls := "auth auth-login box box-pad")(
      h1(trans.signIn()),
      st.form(
        cls := "form3",
        action := s"${routes.Auth.authenticate}${referrer.?? { ref => s"?referrer=${java.net.URLEncoder.encode(ref, "US_ASCII")}" }}",
        method := "post"
      )(
        div(cls := "one-factor")(
          form3.globalError(form),
          auth.bits.formFields(form("username"), form("password"), none, register = false),
          form3.submit(trans.signIn(), icon = none)
        )
      ),
      div(cls := "alternative")(
        a(href := routes.Auth.signup())(trans.signUp())
      )
    )

  }
}
