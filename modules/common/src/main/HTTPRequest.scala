package oyun.common

import play.api.mvc.RequestHeader

object HTTPRequest {
  def isXhr(req: RequestHeader): Boolean =
    (req.headers get "X-Requested-With") contains "XMLHttpRequest"

  def sid(req: RequestHeader): Option[String] = None
}
