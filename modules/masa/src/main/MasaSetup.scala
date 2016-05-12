package oyun.masa

case class MasaSetup(
  rounds: Int,
  variant: Int,
  mode: Option[Int])

object MasaSetup {
  def make(rounds: Int, variant: Int, mode: Option[Int]) = new MasaSetup(rounds, variant, mode)
}
