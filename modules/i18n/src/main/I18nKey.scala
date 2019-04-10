package oyun.i18n

import play.api.i18n.Lang
import play.twirl.api.Html
import oyun.user.UserContext

trait I18nKey {

  val key: String

  def to(lang: Lang)(args: Any*): String

  def en(args: Any*): String = to(I18nKey.en)(args:_*)
}

case class Untranslated(key: String) extends I18nKey {
  def apply(args: Any*)(implicit ctx: UserContext) = Html(key)

  def to(lang: Lang)(args: Any*) = key
}

object I18nKey {
  val en = Lang("en")
  val tr = Lang("tr")
}
