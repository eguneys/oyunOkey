package oyun.app
package templating

import play.api.libs.json.JsObject

import oyun.common.Lang
import oyun.app.ui.ScalatagsTemplate._
import oyun.i18n.{ LangList, I18nKey, Translator, I18nDb, JsDump, TimeagoLocales }
import oyun.user.UserContext

trait I18nHelper {

  // private def pool = i18nEnv.pool

  // lazy val trans = i18nEnv.keys

  implicit def ctxLang(implicit ctx: UserContext): Lang = ctx.lang


  def transKey(key: String, db: I18nDb.Ref, args: Seq[Any] = Nil)(implicit lang: Lang): Frag =
    Translator.frag.literal(key, db, args, lang)

  def i18nJsObject(keys: Seq[I18nKey])(implicit lang: Lang): JsObject =
    JsDump.keysToObject(keys, I18nDb.Site, lang)

  def timeagoLocaleScript(implicit ctx: oyun.api.Context): String = {
    TimeagoLocales.js.get(ctx.lang.code) orElse
    TimeagoLocales.js.get(ctx.lang.language) getOrElse
    ~TimeagoLocales.js.get("en")
  }

  def langName = LangList.name _

  def langNameByStr = LangList.nameByStr _
}
