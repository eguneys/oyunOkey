package oyun.user

import org.joda.time.DateTime

case class User(
  id: String,
  username: String,
  enabled: Boolean,
  seenAt: Option[DateTime],
  createdAt: DateTime,
  lang: Option[String]) {

  def titleUsername = username

}

object User {

  type ID = String

  val anonymous = "Anonymous"

  import oyun.db.BSON.BSONJodaDateTimeHandler

  def normalize(username: String) = username.toLowerCase

  object BSONFields {
    val id = "_id"
    val username = "username"
    val email = "email"
    val enabled = "enabled"
    val createdAt = "createdAt"
    val seenAt = "seenAt"
    val lang = "lang"
    val mustConfirmEmail = "mustConfirmEmail"
  }

  import oyun.db.BSON

  implicit val userBSONHandler = new BSON[User] {

    import BSONFields._
    import reactivemongo.bson.BSONDocument

    def reads(r: BSON.Reader): User = User(
      id = r str id,
      username = r str username,
      enabled = r bool enabled,
      createdAt = r date createdAt,
      seenAt = r dateO seenAt,
      lang = r strO lang)

    def writes(w: BSON.Writer, o: User) = BSONDocument(
      id -> o.id,
      username -> o.username,
      enabled -> o.enabled,
      createdAt -> o.createdAt,
      seenAt -> o.seenAt,
      lang -> o.lang)

  }
}
