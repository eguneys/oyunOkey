package views.html
package auth

import play.api.data.Form

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import controllers.routes

object signup {

  def apply(form: Form[_], recaptchaPublicKey: String)(implicit ctx: Context) =
    views.html.base.layout(
      title = trans.signUp.txt(),
      moreJs = frag(
        jsTag("signup.js")
      ),
      moreCss = cssTag("auth")
    ) {
      main(cls := "auth auth-signup box box-pad")(
        h1(trans.signUp()),
        st.form(
          id := "signup_form",
          cls := "form3",
          action := routes.Auth.signupPost,
          method := "post"
        )(
          auth.bits.formFields(form("username"), form("password"), form("email").some, register = true),
          br
        ),
        form3.submit(trans.signUp(), icon = none, klass="big")
      )
    }
}
