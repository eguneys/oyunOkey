package oyun.common

import java.util.regex.Matcher.quoteReplacement

object String {

  final class Delocalizer(netDomain: String) {
    private val regex = ("""\w{2}\.""" + quoteReplacement(netDomain)).r

    def apply(url: String) = regex.replaceAllIn(url, netDomain)
  }
  
}
