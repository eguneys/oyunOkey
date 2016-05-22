package oyun.i18n

object LangList {

  def name(code: String) = all get code

  def nameOrCode(code: String) = name(code) | code

  val all = Map(
    "en" -> "English",
    "tr" -> "Türkçe")
}
