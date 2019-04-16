package oyun.i18n

import play.twirl.api.Html
import scalatags.Text.RawFrag

import oyun.common.Lang

trait I18nKey {

  val key: String

  def literalHtmlTo(lang: Lang, args: Seq[Any] = Seq.empty): Html

  def literalTxtTo(lang: Lang, args: Seq[Any] = Seq.empty): String

  def literalFragTo(lang: Lang, args: Seq[Any] = Seq.empty): RawFrag

  def to(lang: Lang)(args: Any*): String

  def en(args: Any*): String = to(I18nKey.en)(args:_*)

  def apply(args: Any*)(implicit lang: Lang): Html = literalHtmlTo(lang, args)

  def frag(args: Any*)(implicit lang: Lang): RawFrag = literalFragTo(lang, args)

  def txt(args: Any*)(implicit lang: Lang): String = literalTxtTo(lang, args)
}

case class Untranslated(key: String) extends I18nKey {

  def literalHtmlTo(lang: Lang, args: Seq[Any]): Html = Html(key)

  def to(lang: Lang)(args: Any*) = key

  def literalFragTo(lang: Lang, args: Seq[Any]) = RawFrag(key)

  def literalTxtTo(lang: Lang, args: Seq[Any]) = key
}

final class Translated(val key: String, val db: I18nDb.Ref) extends I18nKey {

  def literalHtmlTo(lang: Lang, args: Seq[Any] = Nil): Html =
    Translator.html.literal(key, db, args, lang)

  def to(lang: Lang)(args: Any*) = key

  def literalFragTo(lang: Lang, args: Seq[Any] = Nil): RawFrag =
    Translator.frag.literal(key, db, args, lang)

  def literalTxtTo(lang: Lang, args: Seq[Any] = Nil): String =
    Translator.txt.literal(key, db, args, lang)
}

object I18nKey {
  val en = Lang("en")
  val tr = Lang("tr")
}
