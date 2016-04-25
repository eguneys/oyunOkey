package oyun.security

import play.api.libs.json._

final class DisposableEmailDomain(
  providerUrl: String,
  busOption: Option[oyun.common.Bus]) {

  private type Matcher = String => Boolean

  private var matchers = List.empty[Matcher]

  private[security] def setDomains(json: JsValue): Unit = try {
    val ds = json.as[List[String]]
    matchers = ds.map { d =>
      val regex = s"""(.+\\.|)${d.replace(".", "\\.")}"""
      makeMatcher(regex)
    }
    failed = false
  }
  catch {
    case e: Exception => onError(e)
  }

  private var failed = false

  private def onError(e: Exception) {
    logger.error("Can't update disposable emails", e)
    if (!failed) {
      failed = true
    }
  }

  private def makeMatcher(regex: String): Matcher = {
    val matcher = regex.r.pattern matcher _
    (s: String) => matcher(s).matches
  }

  def apply(domain: String) = matchers exists { _(domain) }
}
