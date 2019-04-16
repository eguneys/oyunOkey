package oyun.i18n

import oyun.common.Lang

object LangList {

  def name(lang: Lang): String = all.getOrElse(lang, lang.code)

  def nameByStr(str: String): String = I18nLangPicker.byStr(str).fold(str)(name)

  val all = Map(
    Lang("en") -> "English",
    Lang("tr") -> "Türkçe")
}
