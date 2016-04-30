package oyun.app
package templating

import play.twirl.api.Html

import oyun.common.LightUser
import oyun.user.{ User }

trait UserHelper { self: I18nHelper =>
  def lightUser(userId: String): Option[LightUser] = Env.user lightUser userId

  def usernameOrAnon(userId: Option[String]) = (userId flatMap(lightUser(_))).fold(User.anonymous)(_.name)

  def isOnline(userId: String) = Env.user isOnline userId

  def userInfosLink(userId: String) = {
    // val href = userHref(name)

    // Html(s"<a $href>$content</a>""")
    Html(s"User Link")
  }
}
