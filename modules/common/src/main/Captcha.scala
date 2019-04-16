package oyun.common

import scalaz.NonEmptyList

case class Captcha(
  id: String,
  solutions: Captcha.Solutions
) {

  def valid(solution: String) = solutions.toList contains solution

}

object Captcha {

  type Solutions = NonEmptyList[String]

  val failMessage = "captcha.fail"

  def isFailed(form: Form.FormLike) =
    form.errors.exists { _.messages contains failMessage }

}
