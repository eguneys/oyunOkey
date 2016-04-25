package oyun.security

import oyun.user.{ User, UserRepo }

trait EmailConfirm {

  def effective: Boolean

  def send(user: User, email: String): Funit

  def confirm(token: String): Fu[Option[User]]
}

object EmailConfirmSkip extends EmailConfirm {

  def effective = false

  def send(user: User, email: String) = UserRepo setEmailConfirmed user.id

  def confirm(token: String): Fu[Option[User]] = fuccess(none)

}
