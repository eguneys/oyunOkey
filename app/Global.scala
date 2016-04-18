package oyun.app

import play.api.{ Application, GlobalSettings }

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    kamon.Kamon.start()
    oyun.app.Env.current
  }

  override def onStop(app: Application) {
    kamon.Kamon.shutdown()
  }

}
