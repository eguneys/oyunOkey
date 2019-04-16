package oyun.app
package templating

import play.twirl.api.Html

import okey.{ Status => S }

import oyun.game.{ Game, Player, Pov, Namer }
import oyun.user.{ User, UserContext }
import oyun.i18n.{ I18nKeys }

trait GameHelper { self: I18nHelper with UserHelper =>

  def povOpenGraph(pov: Pov) = oyun.app.ui.OpenGraph(
    title = "titleGame(pov.game)",
    //url = s"$netBaseUrl${routes.Round.watcher(pov.game.id, pov.side.name).url}",
    url = "$netBaseUrl",
    description = "describePov")

  // def describePov(pov: Pov) = {
  //   import pov._
  //   // val p1 = playerText(player,
  // }

  def gameVsText(game: Game): String =
    Namer.gameVsText(game)(lightUser)

  def playerLink(
    player: Player,
    withOnline: Boolean = true)(implicit ctx: UserContext) = Html {

    player.userId match {
      case _ =>
        val klass = ""
        val content = User.anonymous
        s"""<span class="user_link$klass">$content</span>"""
    }

  }

  def gameEndStatus(game: Game)(implicit ctx: UserContext): Html = game.status match {
    case S.Aborted => I18nKeys.gameAborted()
    case S.NormalEnd => I18nKeys.gameFinished()
    case S.MiddleEnd => I18nKeys.gameMiddleFinished()
    case _ => I18nKeys.gameFinished()
  }

  def gameEndWinner(game: Game)(implicit ctx: UserContext): Html = {
    val winner = usernameOrAnon(game.winner flatMap (_.userId))

    I18nKeys.gameEndBy(winner)
  }
}
