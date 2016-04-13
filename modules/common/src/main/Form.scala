package oyun.common

import play.api.data.format.Formats._
import play.api.data.Forms._

object Form {

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
