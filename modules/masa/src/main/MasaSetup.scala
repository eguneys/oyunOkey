package oyun.masa

case class MasaSetup(
  rounds: Int,
  variant: Int,
  mode: Option[Int],
  allowAnon: Boolean)

object MasaSetup {
  def make(rounds: Int, variant: Int, mode: Option[Int], allowAnon: Boolean) = new MasaSetup(rounds, variant, mode, allowAnon)
}
