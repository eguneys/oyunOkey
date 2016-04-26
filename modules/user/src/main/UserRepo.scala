package oyun.user

import com.roundeights.hasher.Implicits._
import org.joda.time.DateTime
import reactivemongo.api._
import reactivemongo.bson._

import oyun.db.dsl._

object UserRepo {

  import User.userBSONHandler

  import User.ID
  import User.{ BSONFields => F }

  private val coll = Env.current.userColl

  val normalize = User normalize _

  def byId(id: ID): Fu[Option[User]] = coll.byId[User](id)

  def idByEmail(email: String): Fu[Option[String]] =
    coll.primitiveOne[String]($doc(F.email -> email), "_id")

  def named(username: String): Fu[Option[User]] = coll.byId[User](normalize(username))

  val enabledSelect = $doc(F.enabled -> true)

  def authenticateById(id: ID, password: String): Fu[Option[User]] =
    checkPasswordById(id, password) flatMap { _ ?? coll.byId[User](id) }


  def authenticateByEmail(email: String, password: String): Fu[Option[User]] = fuccess(None)


  private case class AuthData(password: String, salt: String, enabled: Boolean, sha512: Option[Boolean]) {
    def compare(p: String) = password == (~sha512).fold(hash512(p, salt), hash(p, salt))
  }

  private implicit val AuthDataBSONHandler = Macros.handler[AuthData]

  def checkPasswordById(id: ID, password: String): Fu[Boolean] =
    checkPassword($id(id), password)

  private def checkPassword(select: Bdoc, password: String): Fu[Boolean] =
    coll.uno[AuthData](select) map {
      _ ?? ( data  => data.enabled && data.compare(password))
    }

  def create(username: String, password: String, email: Option[String], mobileApiVersion: Option[Int]): Fu[Option[User]] =
    !nameExists(username) flatMap {
      _ ?? {
        coll.insert(newUser(username, password, email)) >>
          named(normalize(username))
      }
    }

  def nameExists(username: String): Fu[Boolean] = idExists(normalize(username))
  def idExists(id: String): Fu[Boolean] = coll exists $id(id)

  def setSeenAt(id: ID) {
    coll.updateFieldUnchecked($id(id), "seenAt", DateTime.now)
  }

  def mustConfirmEmail(id: String): Fu[Boolean] =
    coll.exists($id(id) ++ $doc(F.mustConfirmEmail $exists true))

  def setEmailConfirmed(id: String): Funit = coll.update($id(id), $unset(F.mustConfirmEmail)).void

  private def newUser(username: String, password: String, email: Option[String]) = {

    import oyun.db.BSON.BSONJodaDateTimeHandler

    val salt = ornicar.scalalib.Random nextStringUppercase 32

    $doc(
      F.id -> normalize(username),
      F.username -> username,
      F.email -> email,
      F.mustConfirmEmail -> (email.isDefined).option(DateTime.now),
      "password" -> hash(password, salt),
      "salt" -> salt,
      F.enabled -> true,
      F.createdAt -> DateTime.now,
      F.seenAt -> DateTime.now)
  }

  private def hash(pass: String, salt: String): String = "%s{$s}".format(pass, salt).sha1
  private def hash512(pass: String, salt: String): String = "%s{%s}".format(pass, salt).sha512
}
