package oyun.common

import play.api.http.HeaderNames
import play.api.mvc.RequestHeader

object HTTPRequest {
  def isXhr(req: RequestHeader): Boolean =
    (req.headers get "X-Requested-With") contains "XMLHttpRequest"

  def isSocket(req: RequestHeader): Boolean =
    (req.headers get HeaderNames.UPGRADE) ?? (_.toLowerCase == "websocket")

  def isSynchronousHttp(req: RequestHeader) = !isXhr(req) && !isSocket(req)

  def isSafe(req: RequestHeader) = req.method == "GET"

  def isRedirectable(req: RequestHeader) = isSynchronousHttp(req) && isSafe(req)

  def userAgent(req: RequestHeader): Option[String] = req.headers get HeaderNames.USER_AGENT

  def lastRemoteAddress(req: RequestHeader): String =
    req.remoteAddress.split(", ").lastOption | req.remoteAddress

  def sid(req: RequestHeader): Option[String] = None

  private val fileExtensionPattern = """.+\.[a-z0-9]{2,4}$""".r.pattern

  def hasFileExtension(req: RequestHeader) =
    fileExtensionPattern.matcher(req.path).matches
}
