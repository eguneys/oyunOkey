package oyun.user

import com.roundeights.hasher.Implicits._
import org.joda.time.DateTime
import reactivemongo.api._
import reactivemongo.bson._

import oyun.db.dsl._
import oyun.rating.{ GliOkey, Perf, PerfType }

object UserRepo {

  import User.userBSONHandler

  import User.ID
  import User.{ BSONFields => F }

  private val coll = Env.current.userColl

  val normalize = User normalize _

  def byId(id: ID): Fu[Option[User]] = coll.byId[User](id)

  def idByEmail(email: String): Fu[Option[String]] =
    coll.primitiveOne[String]($doc(F.email -> email), "_id")

  def pair(ids: okey.Sides[Option[ID]]): Fu[okey.Sides[Option[User]]] =
    coll.byIds[User](ids.flatten) map { users =>
      ids map { _.??(x => users.find(_.id == x)) }
    }

  def named(username: String): Fu[Option[User]] = coll.byId[User](normalize(username))

  val oyunkeyfId = "oyunkeyf"
  def oyunkeyf = byId(oyunkeyfId)

  def setPerfs(user: User, perfs: Perfs, prev: Perfs) = {
    val diff = PerfType.all flatMap { pt =>
      perfs(pt).nb != prev(pt).nb option {
        s"perfs.${pt.key}" -> Perf.perfBSONHandler.write(perfs(pt))
      }
    }
    diff.nonEmpty ?? coll.update(
      $id(user.id),
      $doc("$set" -> $doc(diff))
    ).void
  }

  val enabledSelect = $doc(F.enabled -> true)

  def authenticateById(id: ID, password: String): Fu[Option[User]] =
    checkPasswordById(id, password) flatMap { _ ?? coll.byId[User](id) }


  def authenticateByEmail(email: String, password: String): Fu[Option[User]] = fuccess(None)


  def incNbGames(id: ID, rated: Boolean, ai: Boolean, result: Int) = {
    val incs: List[(String, BSONInteger)] = List(
      "count.game".some,
      rated option "count.rated",
      ai option "count.ai"
      // s"count.standing${result}".some
    ).flatten.map(_ -> BSONInteger(1))

    coll.update($id(id), $inc(incs)) void 
  }

  def incNbMasas(id: ID, result: Int) = {
    val incs: List[(String, BSONInteger)] = List(
      "count.masa".some,
      s"count.standing${result}".some
    ).flatten.map(_ -> BSONInteger(1))

    coll.update($id(id), $inc(incs)) void 
  }

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

  def perfOf(id: ID, perfType: PerfType): Fu[Option[Perf]] = coll.find(
    $id(id),
    $doc(s"${F.perfs}.${perfType.key}" -> true)
  ).uno[Bdoc].map {
    _.flatMap(_.getAs[Bdoc](F.perfs)).flatMap(_.getAs[Perf](perfType.key))
  }

  def setSeenAt(id: ID) {
    coll.updateFieldUnchecked($id(id), "seenAt", DateTime.now)
  }

  def mustConfirmEmail(id: String): Fu[Boolean] =
    coll.exists($id(id) ++ $doc(F.mustConfirmEmail $exists true))

  def setEmailConfirmed(id: String): Funit = coll.update($id(id), $unset(F.mustConfirmEmail)).void

  private def newUser(username: String, password: String, email: Option[String]) = {

    implicit def countHandler = Count.countBSONHandler
    import oyun.db.BSON.BSONJodaDateTimeHandler

    val salt = ornicar.scalalib.Random nextStringUppercase 32

    $doc(
      F.id -> normalize(username),
      F.username -> username,
      F.email -> email,
      F.mustConfirmEmail -> (email.isDefined).option(DateTime.now),
      "password" -> hash(password, salt),
      "salt" -> salt,
      F.count -> Count.default,
      F.enabled -> true,
      F.createdAt -> DateTime.now,
      F.seenAt -> DateTime.now)
  }

  private def hash(pass: String, salt: String): String = "%s{$s}".format(pass, salt).sha1
  private def hash512(pass: String, salt: String): String = "%s{%s}".format(pass, salt).sha512
}
