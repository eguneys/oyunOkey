package oyun.app
package templating

import play.api.i18n.{ Lang }
import oyun.i18n.Env.{ current => i18nEnv }
import oyun.i18n.{ I18nKey }
import oyun.user.UserContext

trait I18nHelper {

  private def pool = i18nEnv.pool

  lazy val trans = i18nEnv.keys

  def i18nJsObject(keys: I18nKey*)(implicit lang: Lang) =
    i18nEnv.jsDump.keysToObject(keys, lang)

  implicit def lang(implicit ctx: UserContext) = pool lang ctx.req
}
