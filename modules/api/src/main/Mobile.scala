package oyun.api

import play.api.http.HeaderNames
import play.api.mvc.RequestHeader

object Mobile {

  object Api {
    def currentVersion = 1

    private val  PathPattern = """^.+/socket/v(\d+)$""".r

    def requestVersion(req: RequestHeader): Option[Int] = {
      val accepts = ~req.headers.get(HeaderNames.ACCEPT)
      if (accepts contains "application/vnd.oyunkeyf.v1+json") some(1)
      else req.path match {
        case PathPattern(version) => parseIntOption(version)
        case _ => None
      }
    }
  }

}
