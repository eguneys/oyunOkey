package oyun.game

import oyun.common.LightUser
import play.twirl.api.Html

object Namer {

  def player(p: Player, withRating: Boolean = true)(implicit lightUser: LightUser.Getter) = Html {
    p.aiLevel.fold(
      p.userId.flatMap(lightUser).fold(oyun.user.User.anonymous) { user =>
        if (withRating) s"${user.name}&nbsp;(${ratingString(p)})"
        else user.name
      }) { level => s"A.I. level $level" }
  }

  def playerText(player: Player)(implicit lightUser: LightUser.Getter): String =
    player.aiLevel.fold(
      player.userId.flatMap(lightUser).fold(player.name | "Anon.") { u =>
        u.name
      }
    ) { level => s"Bot AI" }

  def gameVsText(game: Game)(implicit lightUser: LightUser.Getter): String =
    okey.Side.all map { side => playerText(game.player(side)) } mkString " "

  private def ratingString(p: Player) = p.rating match {
    case Some(rating) => s"$rating"
    case _ => "?"
  }

  def playerString(p: Player, withRating: Boolean = true)(implicit lightUser: String => Option[LightUser]) =
    player(p, withRating)(lightUser).body.replace("&nsbp;", " ")

}
