package oyun.app

import play.api.mvc._
import play.api.mvc.Results._
import play.api.{ Application, GlobalSettings }

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    kamon.Kamon.start()
    oyun.app.Env.current
  }

  override def onStop(app: Application) {
    kamon.Kamon.shutdown()
  }

  override def onRouteRequest(req: RequestHeader): Option[Handler] = {
    Env.i18n.requestHandler(req) orElse super.onRouteRequest(req)
  }

}
