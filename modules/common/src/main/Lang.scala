package oyun.common

import play.api.i18n.{ Lang => PlayLang }

case class Lang(value: PlayLang) extends AnyVal {

  def language = value.language
  def code = value.code
  def toLocale = value.toLocale

}

object Lang {

  def apply(language: String): Lang = Lang(PlayLang(language))
  def apply(language: String, country: String): Lang = Lang(PlayLang(language, country))

  def get(code: String): Option[Lang] = {
    PlayLang get code map apply
  }
  
}
