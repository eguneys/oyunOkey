package oyun.app
package templating

import play.twirl.api.Html

import oyun.common.LightUser
import oyun.user.{ User }

trait UserHelper { self: I18nHelper =>
  def lightUser(userId: String): Option[LightUser] = None

  def usernameOrAnon(userId: Option[String]) = User.anonymous

  def userInfosLink(userId: String) = {
    // val href = userHref(name)

    // Html(s"<a $href>$content</a>""")
    Html(s"User Link")
  }
}
