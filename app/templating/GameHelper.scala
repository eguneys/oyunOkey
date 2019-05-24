package oyun.app
package templating

import okey.{ Status => S }

import oyun.game.{ Game, Player, Pov, Namer }
import oyun.user.{ User, UserContext }
import oyun.i18n.{ I18nKeys }

import controllers.routes

import oyun.app.ui.ScalatagsTemplate._

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

  def playerUsername(player: Player): Frag =
    player.aiLevel.fold[Frag](
      player.userId.flatMap(lightUser).fold[Frag](oyun.user.User.anonymous) { user =>
        frag(user.name)
      }
    ) { level => raw(s"Bot $level") }

  def gameVsText(game: Game): String =
    Namer.gameVsText(game)(lightUser)

  def playerLink(
    player: Player,
    cssClass: Option[String] = None,
    withOnline: Boolean = true)(implicit ctx: UserContext): Frag = {

    player.userId.flatMap(lightUser) match {
      case None =>
        val klass = ""
        span(cls := s"user_link$klass")(
          (player.aiLevel) match {
            case Some(level) => s"Bot $level"
            case _ => User.anonymous
          }
        )
      case Some(user) => frag(
        a(cls := userClass(user.id, cssClass, withOnline)
          //href := s"${routes.User show user.name}"
        )(
          withOnline option frag(lineIcon(user), " "),
            playerUsername(player)
        )
      )
    }

  }

  def gameEndStatus(game: Game)(implicit ctx: UserContext): Frag = game.status match {
    case S.Aborted => I18nKeys.gameAborted()
    case S.NormalEnd => I18nKeys.gameFinished()
    case S.MiddleEnd => I18nKeys.gameMiddleFinished()
    case _ => I18nKeys.gameFinished()
  }

  def gameEndWinner(game: Game)(implicit ctx: UserContext): Frag = {
    val winner = usernameOrAnon(game.winner flatMap (_.userId))

    I18nKeys.gameEndBy(winner)
  }
}
