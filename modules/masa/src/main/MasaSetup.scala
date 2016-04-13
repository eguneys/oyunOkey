package oyun.masa

case class MasaSetup(
  rounds: Int,
  variant: Int)

object MasaSetup {
  def make(rounds: Int, variant: Int) = new MasaSetup(rounds, variant)
}
