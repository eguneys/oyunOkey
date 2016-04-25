package oyun.app
package templating

import play.api.data._
import play.twirl.api.Html
import oyun.api.Context

trait FormHelper { self: I18nHelper =>

  private val errNames = Map(
    "error.minLength" -> trans.textIsTooShort,
    "error.maxLength" -> trans.textIsTooLong,
    "captcha.fail" -> trans.invalidCaptcha)

  def errMsg(form: Field)(implicit ctx: Context): Html = errMsg(form.errors)
  def errMsg(form: Form[_])(implicit ctx: Context): Html = errMsg(form.errors)

  def errMsg(errors: Seq[FormError])(implicit ctx: Context): Html = Html {
    errors map { e =>
      val msg = transKey(e.message, e.args) match {
        case m if m == e.message => errNames.get(e.message).fold(e.message)(_.str())
        case m => m
      }
      s"""<p class="error">$msg</p>"""
    } mkString
  }

}
