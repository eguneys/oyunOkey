package oyun.app
package templating

import oyun.i18n.Env.{ current => i18nEnv }

import oyun.user.UserContext

trait I18nHelper {

  private def pool = i18nEnv.pool

  lazy val trans = i18nEnv.keys

  implicit def lang(implicit ctx: UserContext) = pool lang ctx.req
}
