package oyun.i18n

import scalatags.Text.RawFrag

import oyun.common.Lang

trait I18nKey {

  val key: String

  def literalTxtTo(lang: Lang, args: Seq[Any] = Seq.empty): String

  def literalTo(lang: Lang, args: Seq[Any] = Seq.empty): RawFrag

  def to(lang: Lang)(args: Any*): String

  def en(args: Any*): String = to(I18nKey.en)(args:_*)

  def apply(args: Any*)(implicit lang: Lang): RawFrag = literalTo(lang, args)

  def txt(args: Any*)(implicit lang: Lang): String = literalTxtTo(lang, args)
}

case class Untranslated(key: String) extends I18nKey {

  def to(lang: Lang)(args: Any*) = key

  def literalTo(lang: Lang, args: Seq[Any]) = RawFrag(key)

  def literalTxtTo(lang: Lang, args: Seq[Any]) = key
}

final class Translated(val key: String, val db: I18nDb.Ref) extends I18nKey {

  def to(lang: Lang)(args: Any*) = key

  def literalTo(lang: Lang, args: Seq[Any] = Nil): RawFrag =
    Translator.frag.literal(key, db, args, lang)

  def literalTxtTo(lang: Lang, args: Seq[Any] = Nil): String =
    Translator.txt.literal(key, db, args, lang)
}

object I18nKey {
  val en = Lang("en")
  val tr = Lang("tr")
}
