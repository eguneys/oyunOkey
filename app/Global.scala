package oyun.app

import play.api.{ Application, GlobalSettings }

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    oyun.app.Env.current
  }

}
