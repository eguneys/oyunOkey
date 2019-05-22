package oyun.setup

import akka.actor._

final class Env(
  masaApi: oyun.masa.MasaApi,
  system: ActorSystem) {

  lazy val forms = new FormFactory()

  lazy val processor = new Processor(
    bus = system.oyunBus,
    masaApi = masaApi
  )
}

object Env {
  lazy val current = new Env(
    system = oyun.common.PlayApp.system,
    masaApi = oyun.masa.Env.current.api
  )
}
