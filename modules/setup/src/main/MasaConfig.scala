package oyun.setup

import oyun.masa.MasaSetup
import oyun.user.User

case class MasaConfig(ratingRange: Option[String]) {

  def masa(): MasaSetup = MasaSetup.make
}
