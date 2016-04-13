package oyun.setup

import oyun.masa.MasaSetup
import oyun.user.User

case class MasaConfig(
  rounds: Int,
  variant: Int,
  ratingRange: Option[String]) extends HumanConfig {

  def masa(): MasaSetup = MasaSetup.make(
    rounds = rounds,
    variant = variant)
}

object MasaConfig extends BaseHumanConfig {
}
