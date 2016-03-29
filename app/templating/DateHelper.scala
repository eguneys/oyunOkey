package oyun.app
package templating

import java.util.Locale
import scala.collection.mutable

import org.joda.time.format._
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ DateTime }
import play.twirl.api.Html

import oyun.api.Context

trait DateHelper { self: I18nHelper =>

  private val dateTimeStyle = "MS"

  private val dateTimeFormatters = mutable.Map[String, DateTimeFormatter]()

  private val isoFormatter = ISODateTimeFormat.dateTime

  private def dateTimeFormatter(ctx: Context): DateTimeFormatter =
    dateTimeFormatters.getOrElseUpdate(
      lang(ctx).language,
      DateTimeFormat forStyle dateTimeStyle withLocale new Locale(lang(ctx).language))


  def showDateTime(date: DateTime)(implicit ctx: Context): String =
    dateTimeFormatter(ctx) print date

  def momentFormat(date: DateTime, format: String): Html = Html {
    s"""<time class="moment" datetime="${isoDate(date)}" data-format="$format"></time>"""
  }

  def momentFormat(date: DateTime): Html = momentFormat(date, "calendar")

  def isoDate(date: DateTime): String = isoFormatter print date
}
