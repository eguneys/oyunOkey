package oyun.masa

import oyun.game.{ Mode }

case class MasaSetup(
  rounds: Int,
  variant: Int,
  mode: Option[Int],
  allowAnon: Boolean) {

  val realMode = Mode orDefault (mode | 0)

  def membersOnly = !allowAnon || realMode.rated

  def compatibleWith(s: MasaSetup) = {
    println(this, s)
    compatibilityProperties == s.compatibilityProperties &&
      (membersOnly || s.membersOnly).fold(membersOnly && s.membersOnly, true)
  }

  def compatibilityProperties = (variant, rounds, realMode)

}

object MasaSetup {
  def make(rounds: Int, variant: Int, mode: Option[Int], allowAnon: Boolean) = new MasaSetup(rounds, variant, mode, allowAnon)
}
