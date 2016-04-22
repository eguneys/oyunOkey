package oyun.security

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints

import oyun.common.LameName
import oyun.user.{ User, UserRepo }

final class DataForm(emailAddress: EmailAddress) {
  import DataForm._

  private val anyEmail = nonEmptyText.verifying(Constraints.emailAddress)
  private val acceptableEmail = anyEmail.verifying(emailAddress.acceptableConstraint)
  private def acceptableUniqueEmail(forUser: Option[User]) =
    acceptableEmail.verifying(emailAddress uniqueConstraint forUser)

  object signup {

    private val username = nonEmptyText.verifying(
      Constraints minLength 2,
      Constraints maxLength 20,
      Constraints.pattern(
        regex = """^[\w-]+$""".r,
        error = "Invalid username. Please use only letters, numbers and dash"),
      Constraints.pattern(
        regex = """^[^\d].+$""".r,
        error = "The username must not start with a number")
      ).verifying("This user already exists", u => !UserRepo.nameExists(u).awaitSeconds(2))
      .verifying("This username is not acceptable", u => !LameName(u))

    val website = Form(mapping(
      "username" -> username,
      "password" -> text(minLength = 4),
      "email" -> acceptableUniqueEmail(none),
      "g-recaptcha-response" -> nonEmptyText
    )(SignupData.apply)(_ => None)
      //.verifying(captchaFailMessage, validateCaptcha _)
    )

    // def websiteWithCaptcha = website

  }
}

object DataForm {

  case class SignupData(
    username: String,
    password: String,
    email: String,
    `g-recaptcha-response`: String) {
    def recaptchaResponse = `g-recaptcha-response`
  }
}
