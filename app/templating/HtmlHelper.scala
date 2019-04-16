package oyun.app
package templating

import ornicar.scalalib.Zero
import play.twirl.api.Html

trait HtmlHelper {

  val emptyHtml = Html("")

  implicit val OyunHtmlZero: Zero[Html] = Zero.instance(emptyHtml)

}
