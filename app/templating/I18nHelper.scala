package oyun.app
package templating

import play.api.i18n.{ Lang }
import oyun.i18n.Env.{ current => i18nEnv }
import oyun.i18n.{ LangList, I18nKey }
import oyun.user.UserContext

trait I18nHelper {

  private def pool = i18nEnv.pool

  lazy val trans = i18nEnv.keys

  implicit def lang(implicit ctx: UserContext) = pool lang ctx.req


  def transKey(key: String, args: Seq[Any] = Nil)(implicit lang: Lang): String =
    i18nEnv.translator.transTo(key, args)(lang)

  def i18nJsObject(keys: I18nKey*)(implicit lang: Lang) =
    i18nEnv.jsDump.keysToObject(keys, lang)

  def langName(lang: Lang): Option[String] = langName(lang.language)
  def langName(lang: String): Option[String] = LangList name lang
}
