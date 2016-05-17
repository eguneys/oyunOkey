package oyun.app
package templating

import org.apache.commons.lang3.StringEscapeUtils.escapeHtml4
import play.twirl.api.Html

import oyun.user.{ User, UserContext }

trait StringHelper { self: NumberHelper =>

  def netDomain: String

  // the replace quot; -> " is required
  // to avoid issues caused by addLinks
  // when an url is surrounded by quotes
  def escape(text: String) = escapeEvenDoubleQuotes(text).replace("&quot;", "\"")
  def escapeEvenDoubleQuotes(text: String) = escapeHtml4(text)

  def nl2br(text: String) = text.replace("\r\n", "<br />").replace("\n", "<br />")

  private val markdownLinkRegex = """\[([^\[]+)\]\(([^\)]+)\)""".r

  def markdownLinks(text: String) = Html {
    nl2br {
      markdownLinkRegex.replaceAllIn(escape(text), m => {
        s"""<a href="${m group 2}">${m group 1}</a>"""
      })
    }
  }

  private val NumberFirstRegex = """^(\d+)\s(.+)$""".r
  private val NumberLastRegex = """^(.+)\s(\d+)$""".r
  def splitNumber(s: String)(implicit ctx: UserContext): Html = Html {
    s match {
      case NumberFirstRegex(number, text) => "<strong>%s</strong><br />%s".format((~parseIntOption(number)).localize, text)
      case NumberLastRegex(text, number) => "%s<br /><strong>%s</strong>".format(text, (~parseIntOption(number)).localize)
      case h => h.replace("\n", "<br />")
    }
  }

  def splitNumber(s: Html)(implicit ctx: UserContext): Html = splitNumber(s.body)
}
