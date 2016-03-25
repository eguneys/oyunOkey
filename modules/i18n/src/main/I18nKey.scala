package oyun.i18n

import play.api.i18n.Lang
import play.twirl.api.Html
import oyun.user.UserContext

trait I18nKey {

  val key: String
}

case class Untranslated(key: String) extends I18nKey {
  def apply(args: Any*)(implicit ctx: UserContext) = Html(key)
}

object I18nKey {
  val en = Lang("en")
}
