package oyun.user

case class User(
  id: String,
  username: String)

object User {

  type ID = String

  val anonymous = "Anonymous"

  def normalize(username: String) = username.toLowerCase

  object BSONFields {
    val email = "email"
    val mustConfirmEmail = "mustConfirmEmail"
  }
}
