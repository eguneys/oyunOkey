package oyun.setup

import oyun.masa.MasaSetup
import oyun.user.User

case class AiConfig(
  rounds: Int,
  variant: okey.variant.Variant) extends Config {

  def >> = (rounds, variant.id).some

  def masa(): MasaSetup = MasaSetup.make(
    rounds = rounds,
    variant = variant.id,
    mode = none,
    allowAnon = true
  )

}

object AiConfig extends BaseConfig {

  def <<(r: Int, v: Int) = new AiConfig(
    rounds = r,
    variant = okey.variant.Variant(v) err "Invalid game variant " + v)
}
