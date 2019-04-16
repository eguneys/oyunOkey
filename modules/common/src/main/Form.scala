package oyun.common

import play.api.data.format.Formats._
import play.api.data.Forms._
import play.api.data.{ FormError, Field }

object Form {

  type FormLike = {
    def apply(key: String): Field
    def errors: Seq[FormError]
  }

  def options(it: Iterable[Int], pattern: String) = it map { d =>
    d -> (pattern format d)
  }

  def numberIn(choices: Iterable[(Int, String)]) =
    number.verifying(hasKey(choices, _))

  def hasKey[A](choices: Iterable[(A, _)], key: A) =
    choices.map(_._1).toList contains key

  def errorsAsJson(form: play.api.data.Form[_])(implicit lang: play.api.i18n.Messages) =
    form.errorsAsJson
}
