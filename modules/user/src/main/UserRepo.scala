package oyun.user

import oyun.db.dsl._

object UserRepo {

  import User.ID
  import User.{ BSONFields => F }

  private val coll = Env.current.userColl

  val normalize = User normalize _

  def idByEmail(email: String): Fu[Option[String]] =
    coll.primitiveOne[String]($doc(F.email -> email), "_id")

  def authenticateById(id: ID, password: String): Fu[Option[User]] =
    fuccess(None) // checkPasswordById(id, password) flatMap { _ ?? coll.byId[User](id) }


  def authenticateByEmail(email: String, password: String): Fu[Option[User]] = fuccess(None)


  def create(username: String, password: String, email: Option[String], mobileApiVersion: Option[Int]): Fu[Option[User]] =
    fufail("lksa")
    // !nameExists(username) flatMap {
    //   _ ?? {
    //     coll.insert(newUser
    //   }
    // }

  def nameExists(username: String): Fu[Boolean] = idExists(normalize(username))
  def idExists(id: String): Fu[Boolean] = coll exists $id(id)

  def setEmailConfirmed(id: String): Funit = coll.update($id(id), $unset(F.mustConfirmEmail)).void
}
