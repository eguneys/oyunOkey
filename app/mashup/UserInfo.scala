package oyun.app
package mashup

import oyun.api.Context
import oyun.user.{ User }

case class UserInfo() {
}

object UserInfo {

  def apply(
    countUsers: () => Fu[Int])(user: User, ctx: Context): Fu[UserInfo] =
    funit map(_ => new UserInfo())
}
