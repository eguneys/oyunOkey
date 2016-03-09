package oyun.masa

import ornicar.scalalib.Random

case class Masa(id: String) {

  def fullName =
    s"name"

}

object Masa {

  def make() = Masa(
    id = Random nextStringUppercase 8
  )
}
