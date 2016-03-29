package oyun.app
package templating

import oyun.common.LightUser

trait UserHelper { self: I18nHelper =>
  def lightUser(userId: String): Option[LightUser] = None
}
