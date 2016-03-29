package oyun.app
package templating

import play.twirl.api.Html

import oyun.game.{ Game, Player, Pov }
import oyun.user.{ User, UserContext }

trait GameHelper { self: I18nHelper with UserHelper =>

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
}
