package oyun.app

import play.api.mvc._
import play.api.mvc.Results._
import play.api.{ Application, GlobalSettings }

import oyun.common.HTTPRequest

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    kamon.Kamon.start()
    oyun.app.Env.current
  }

  override def onStop(app: Application) {
    kamon.Kamon.shutdown()
  }

  override def onRouteRequest(req: RequestHeader): Option[Handler] = {
    // Env.i18n.requestHandler(req) orElse
    super.onRouteRequest(req)
  }

  private def niceError(req: RequestHeader): Boolean =
    req.method == "GET" &&
      HTTPRequest.isSynchronousHttp(req) &&
      !HTTPRequest.hasFileExtension(req)

  override def onHandlerNotFound(req: RequestHeader) =
    if (niceError(req)) controllers.Main.notFound(req)
    else fuccess(NotFound("404 - Resource not found"))

}
