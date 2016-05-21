package oyun.app
package templating

import play.twirl.api.Html

import okey.{ Status => S }

import oyun.game.{ Game, Player, Pov }
import oyun.user.{ User, UserContext }

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
    case S.Aborted => trans.gameAborted()
    case S.NormalEnd => trans.gameFinished()
    case S.MiddleEnd => trans.gameMiddleFinished()
    case _ => trans.gameFinished()
  }

  def gameEndWinner(game: Game)(implicit ctx: UserContext): Html = {
    val winner = usernameOrAnon(game.winner flatMap (_.userId))

    trans.gameEndBy(winner)
  }
}
