package oyun.app
package templating

import java.text.NumberFormat
import java.util.Locale
import scala.collection.mutable

import oyun.user.UserContext

trait NumberHelper { self: I18nHelper =>

  private val formatters = mutable.Map[String, NumberFormat]()

  private def formatter(implicit ctx: UserContext): NumberFormat =
    formatters.getOrElseUpdate(
      ctx.lang.language,
      NumberFormat getInstance new Locale(ctx.lang.language))

  implicit def richInt(number: Int) = new {
    def localize(implicit ctx: UserContext): String = formatter format number
  }

}
