package oyun.user

case class User(
  id: String,
  username: String)

object User {
  val anonymous = "Anonymous"
}
