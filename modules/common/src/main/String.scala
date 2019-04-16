package oyun.common

import java.text.Normalizer
import java.util.regex.Matcher.quoteReplacement

import play.twirl.api.Html
import scalatags.Text.RawFrag

import oyun.common.base.StringUtils.{ escapeHtml => escapeHtmlRaw }

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

    def escapeHtml(s: String) = Html {
      escapeHtmlRaw(s)
    }

  }

  object frag {
    def escapeHtml(s: String) = RawFrag {
      escapeHtmlRaw(s)
    }
  }
}
