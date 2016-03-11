package oyun.common

import java.util.regex.Matcher.quoteReplacement

import play.api.mvc.{ Cookie, Session, RequestHeader }

object OyunCookie {

  private val domainRegex = """^.+(\.[^\.]+\.[^\.]+)$""".r

  private def domain(req: RequestHeader): String = {
    domainRegex.replaceAllIn(req.domain, m => quoteReplacement(m group 1))
    req.domain
  }

  def cookie(name: String, value: String, maxAge: Option[Int] = None, httpOnly: Option[Boolean] = None)(implicit req: RequestHeader): Cookie = Cookie(
    name,
    value,
    maxAge orElse Session.maxAge orElse 86400.some,
    "/",
    domain(req).some,
    Session.secure,
    httpOnly | Session.httpOnly)
}
