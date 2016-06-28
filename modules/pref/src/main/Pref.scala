package oyun.pref

import oyun.user.User

case class Pref(
  _id: String, // user id
  theme: String) {

  import Pref._

  def id = _id


}

object Pref {

  def create(id: String) = default.copy(_id = id)

  lazy val default = Pref(
    _id = "",
    theme = "")
}
