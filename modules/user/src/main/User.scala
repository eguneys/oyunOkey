package oyun.user

import scala.concurrent.duration._

import org.joda.time.DateTime
import oyun.rating.PerfType

case class User(
  id: String,
  username: String,
  perfs: Perfs,
  count: Count,
  troll: Boolean = false,
  enabled: Boolean,
  seenAt: Option[DateTime],
  createdAt: DateTime,
  lang: Option[String]) {

  override def toString =
    s"User $username(games:${count.game})${troll ?? " troll"}"

  def noTroll = !troll

  def disabled = !enabled


  def lame = false

  // TODO
  def usernameWithBestRating = s"$username"

  def titleUsername = username

  def titleUsernameWithBestRating = usernameWithBestRating


  def hasGames = count.game > 0

  def countRated = count.rated

  def seenRecently: Boolean = timeNoSee < 2.minutes

  def timeNoSee: Duration = seenAt.fold[Duration](Duration.Inf) { s =>
    (nowMillis - s.getMillis).millis
  }
}

object User {

  type ID = String

  val anonymous = "Anonymous"

  case class Active(user: User)

  import oyun.db.BSON.BSONJodaDateTimeHandler

  def normalize(username: String) = username.toLowerCase

  object BSONFields {
    val id = "_id"
    val username = "username"
    val perfs = "perfs"
    val count = "count"
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
    private implicit def countHandler = Count.countBSONHandler
    private implicit def perfsHandler = Perfs.perfsBSONHandler

    def reads(r: BSON.Reader): User = User(
      id = r str id,
      username = r str username,
      perfs = r.getO[Perfs](perfs) | Perfs.default,
      count = r.get[Count](count),
      enabled = r bool enabled,
      createdAt = r date createdAt,
      seenAt = r dateO seenAt,
      lang = r strO lang)

    def writes(w: BSON.Writer, o: User) = BSONDocument(
      id -> o.id,
      username -> o.username,
      perfs -> o.perfs,
      count -> o.count,
      enabled -> o.enabled,
      createdAt -> o.createdAt,
      seenAt -> o.seenAt,
      lang -> o.lang)

  }
}
