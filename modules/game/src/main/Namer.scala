package oyun.game

import oyun.common.LightUser
import play.twirl.api.Html

object Namer {

  def playerText(player: Player)(implicit lightUser: LightUser.Getter): String =
    player.aiLevel.fold(
      player.userId.flatMap(lightUser).fold(player.name | "Anon.") { u =>
        u.name
      }
    ) { level => s"Bot AI" }

  def gameVsText(game: Game)(implicit lightUser: LightUser.Getter): String =
    okey.Side.all map { side => playerText(game.player(side)) } mkString " "

}
