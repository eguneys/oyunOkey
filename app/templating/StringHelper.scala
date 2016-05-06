package oyun.app
package templating

import org.apache.commons.lang3.StringEscapeUtils.escapeHtml4
import play.twirl.api.Html

trait StringHelper {

  def netDomain: String

  // the replace quot; -> " is required
  // to avoid issues caused by addLinks
  // when an url is surrounded by quotes
  def escape(text: String) = escapeEvenDoubleQuotes(text).replace("&quot;", "\"")
  def escapeEvenDoubleQuotes(text: String) = escapeHtml4(text)
}
