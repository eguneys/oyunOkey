package oyun.app
package templating

import play.api.libs.json.JsObject
import play.twirl.api.Html

import oyun.common.Lang
import oyun.i18n.{ LangList, I18nKey, Translator, I18nDb, JsDump }
import oyun.user.UserContext

trait I18nHelper {

  // private def pool = i18nEnv.pool

  // lazy val trans = i18nEnv.keys

  implicit def ctxLang(implicit ctx: UserContext): Lang = ctx.lang


  def transKey(key: String, db: I18nDb.Ref, args: Seq[Any] = Nil)(implicit lang: Lang): Html =
    Translator.html.literal(key, db, args, lang)

  def i18nJsObject(keys: Seq[I18nKey])(implicit lang: Lang): JsObject =
    JsDump.keysToObject(keys, I18nDb.Site, lang)

  def langName = LangList.name _

  def langNameByStr = LangList.nameByStr _

  // private lazy val langAnnotationsBase: String =
  //   pool.names.keySet map { code =>
  //     s"""<link rel="alternate" hreflang="$code" href="http://$code.oyunkeyf.net%"/>"""
  //   } mkString ""

  // def langAnnotations(implicit ctx: UserContext) = Html {
  //   langAnnotationsBase.replace("%", ctx.req.uri)
  // }
}
