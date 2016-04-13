package oyun.app
package templating

import oyun.common.LightUser
import oyun.user.{ User }

trait UserHelper { self: I18nHelper =>
  def lightUser(userId: String): Option[LightUser] = None

  def usernameOrAnon(userId: Option[String]) = User.anonymous
}
