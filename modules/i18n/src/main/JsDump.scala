package oyun.i18n

import play.api.i18n.Lang
import play.api.libs.json.{ JsString, JsObject }

private[i18n] final class JsDump(
  path: String,
  pool: I18nPool,
  keys: I18nKeys) {

  def keysToObject(keys: Seq[I18nKey], lang: Lang) = JsObject {
    keys.map { k =>
      k.key -> JsString(k.to(lang)())
    }
  }

  def keysToMessageObject(keys: Seq[I18nKey], lang: Lang) = JsObject {
    keys.map { k =>
      k.en() -> JsString(k.to(lang)())
    }
  }
}
