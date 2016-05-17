package oyun.setup

import oyun.masa.MasaSetup
import oyun.user.User

case class MasaConfig(
  rounds: Int,
  variant: Int,
  mode: Option[Int],
  membersOnly: Boolean,
  ratingRange: Option[String]) extends HumanConfig {

  def masa(): MasaSetup = MasaSetup.make(
    rounds = rounds,
    variant = variant,
    mode = mode,
    allowAnon = !membersOnly)
}

object MasaConfig extends BaseHumanConfig {
}
