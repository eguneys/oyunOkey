package oyun.i18n

import play.api.mvc.RequestHeader
import play.twirl.api.Html
import scalatags.Text.RawFrag

import oyun.common.Lang
import oyun.common.String.html.escapeHtml
import oyun.common.String.frag.{ escapeHtml => escapeFrag }

object Translator {
  object html {
    def literal(key: MessageKey, db: I18nDb.Ref, args: Seq[Any], lang: Lang): Html =
      translate(key, db, lang, I18nQuantity.Other, args)

    private def translate(key: MessageKey, db: I18nDb.Ref, lang: Lang, quantity: I18nQuantity, args: Seq[Any]): Html =
      findTranslation(key, db, lang) flatMap { translation =>
        val htmlArgs = escapeArgs(args)
        try {
          translation match {
            case literal: Simple => Some(literal.formatHtml(htmlArgs))
          }
        } catch {
          case e: Exception =>
            Some(Html(key))
        }
      } getOrElse {
        Html(key)
      }

    private def escapeArgs(args: Seq[Any]): Seq[Html] = args.map {
      case s: String => escapeHtml(s)
      case h: Html => h
      case r: RawFrag => Html(r.v)
      case a => Html(a.toString)
    }
  }

  object frag {
    def literal(key: MessageKey, db: I18nDb.Ref, args: Seq[Any], lang: Lang): RawFrag =
      translate(key, db, lang, I18nQuantity.Other, args)

    private def translate(key: MessageKey, db: I18nDb.Ref, lang: Lang, quantity: I18nQuantity, args: Seq[Any]): RawFrag =
      findTranslation(key, db, lang) flatMap { translation =>
        val htmlArgs = escapeArgs(args)
        try {
          translation match {
            case literal: Simple => Some(literal.formatFrag(htmlArgs))
          }
        } catch {
          case e: Exception =>
            Some(RawFrag(key))
        }
      } getOrElse {
        RawFrag(key)
      }

    private def escapeArgs(args: Seq[Any]): Seq[RawFrag] = args.map {
      case s: String => escapeFrag(s)
      case h: Html => RawFrag(h.body)
      case r: RawFrag => r
      case a => RawFrag(a.toString)
    }
  }

  object txt {

    def literal(key: MessageKey, db: I18nDb.Ref, args: Seq[Any], lang: Lang): String =
      translate(key, db, lang, I18nQuantity.Other, args)

    private def translate(key: MessageKey, db: I18nDb.Ref, lang: Lang, quantity: I18nQuantity, args: Seq[Any]): String =
      findTranslation(key, db, lang) flatMap { translation =>
        try {
          translation match {
            case literal: Simple => Some(literal.formatTxt(args))
        }
      } catch {
          case e: Exception =>
            Some(key)
        }
      } getOrElse {
        key
      }
  }


  private[i18n] def findTranslation(key: MessageKey, db: I18nDb.Ref, lang: Lang): Option[Translation] =
    I18nDb(db).get(lang.value).flatMap(t => Option(t get key)) orElse
  I18nDb(db).get(defaultLang.value).flatMap(t => Option(t get key))
}
