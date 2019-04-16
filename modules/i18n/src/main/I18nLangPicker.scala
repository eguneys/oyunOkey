package oyun.i18n

import play.api.mvc.RequestHeader

import oyun.common.Lang
import oyun.user.User

object I18nLangPicker {

  def apply(req: RequestHeader, user: Option[User]): Lang =
    user
      .flatMap(_.lang)
      .orElse(req.session get "lang")
      .flatMap(Lang.get)
      .flatMap(findCloser)
      .getOrElse(defaultLang)

  def byStr(str: String): Option[Lang] =
    Lang get str flatMap findCloser
  

  def findCloser(to: Lang): Option[Lang] = Some(to)
}
