package views.html
package masa

import play.api.data.{ Field, Form }

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._
import oyun.user.User

import controllers.routes

object form {

  def apply(form: Form[_])(implicit ctx: Context) = views.html.base.layout(
    title = trans.newMasa.txt(),
    moreCss = cssTag("masa.form"),
    moreJs = frag(
      jsTag("masaForm.js")
    )
  )(main(cls := "page-small")(
    
  ))

}
