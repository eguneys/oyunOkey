package oyun.i18n

import play.api.mvc.Results.Redirect
import play.api.mvc.{ Action, RequestHeader, Handler, Result }

import oyun.common.HTTPRequest

final class I18nRequestHandler(
  pool: I18nPool,
  protocol: String,
  cdnDomain: String) {

  def apply(req: RequestHeader): Option[Handler] =
    if (HTTPRequest.isRedirectable(req) &&
      req.host != cdnDomain &&
      pool.domainLang(req).isEmpty) Some(Action(Redirect(redirectUrl(req))))
    else None

  private def redirectUrl(req: RequestHeader) =
    redirectUrlLang(req, pool.preferred(req).language)

  private def redirectUrlLang(req: RequestHeader, lang: String) =
    s"$protocol${I18nDomain(req.domain).withLang(lang).domain}${req.uri}"

}
