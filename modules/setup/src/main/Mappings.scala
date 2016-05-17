package oyun.setup

import play.api.data.Forms._

import oyun.game.Mode

object Mappings {
  def mode(withRated: Boolean) = optional(rawMode(withRated))
  def rawMode(withRated: Boolean) = number
    .verifying(Mode.all map (_.id) contains _)
    .verifying(m => m == Mode.Casual.id || withRated)
  val ratingRange = nonEmptyText
}
