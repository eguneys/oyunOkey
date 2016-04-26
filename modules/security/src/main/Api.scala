package oyun.security

import ornicar.scalalib.Random

import play.api.data._
import play.api.data.Forms._
import play.api.mvc.RequestHeader

import oyun.user.{ User, UserRepo }

final class Api(emailAddress: EmailAddress) {

  val AccessUri = "access_uri"

  def loginForm = Form(mapping(
    "username" -> nonEmptyText,
    "password" -> nonEmptyText
  )(authenticateUser)(_.map(u => (u.username, "")))
    .verifying("Invalid username or password", _.isDefined)
  )

  def saveAuthentication(userId: String)(implicit req: RequestHeader): Fu[String] =
    UserRepo mustConfirmEmail userId flatMap {
      //case true => fufail(Api MustConfirmEmail userId)
      case _ =>
        val sessionId = Random nextStringUppercase 12
        Store.save(sessionId, userId, req) inject sessionId
    }

  // blocking function, required by Play2 form
  private def authenticateUser(usernameOrEmail: String, password: String): Option[User] =
    (emailAddress.validate(usernameOrEmail) match {
      case Some(email) => UserRepo.authenticateByEmail(email, password)
      case None => UserRepo.authenticateById(User normalize usernameOrEmail, password)
    }) awaitSeconds 4

  def restoreUser(req: RequestHeader): Fu[Option[FingerprintedUser]] =
    reqSessionId(req) ?? { sessionId =>
      Store userIdAndFingerprint sessionId flatMap {
        _ ?? { d =>
          UserRepo.byId(d.user) map {
            _ map {
              FingerprintedUser(_, d.fp.isDefined)
            }
          }
        }
      }
    }

  def reqSessionId(req: RequestHeader) = req.session get "sessionId"
}
