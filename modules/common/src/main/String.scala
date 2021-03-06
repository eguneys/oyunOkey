package oyun.common

import java.text.Normalizer
import java.util.regex.Matcher.quoteReplacement
import play.api.libs.json._

import play.twirl.api.Html
import scalatags.Text.all._

import oyun.base.RawHtml
import oyun.common.base.StringUtils.{ safeJsonString, escapeHtmlRaw }

object String {

  private[this] val slugR = """[^\w-]""".r
  private[this] val slugMultiDashRegex = """-{2,}""".r

  def slugify(input: String) = {
    val nowhitespace = input.trim.replace(' ', '-')
    val singleDashes = slugMultiDashRegex.replaceAllIn(nowhitespace, "-")
    val normalized = Normalizer.normalize(singleDashes, Normalizer.Form.NFD)
    val slug = slugR.replaceAllIn(normalized, "")
    slug.toLowerCase
  }

  def shorten(text: String, length: Int, sep: String = "…") = {
    val t = text.replace('\n', ' ')
    if (t.size > (length + sep.size)) (t take length) ++ sep
    else t
  }

  final class Delocalizer(netDomain: String) {
    private val regex = ("""\w{2}\.""" + quoteReplacement(netDomain)).r

    def apply(url: String) = regex.replaceAllIn(url, netDomain)
  }

  object html {

    def richText(rawText: String, nl2br: Boolean = true) = Html {
      val withLinks = rawText // RawHtml.addLinks(rawText)
      if (nl2br) RawHtml.nl2br(withLinks) else withLinks
    }

    def escapeHtml(s: String) = Html {
      escapeHtmlRaw(s)
    }

    def safeJsonValue(jsValue: JsValue): String = {
      jsValue match {
        case JsNull => "null"
        case JsString(s) => safeJsonString(s)
        case JsNumber(n) => n.toString
        case JsBoolean(b) => if (b) "true" else "false"
        case JsArray(items) => items.map(safeJsonValue).mkString("[", ",", "]")
        case JsObject(fields) => {
          fields.map {
            case (k, v) => s"${safeJsonString(k)}:${safeJsonValue(v)}"
          }.mkString("{", ",", "}")
        }
      }
    }

  }

  object frag {
    def escapeHtml(s: String) = RawFrag {
      escapeHtmlRaw(s)
    }
  }
}
