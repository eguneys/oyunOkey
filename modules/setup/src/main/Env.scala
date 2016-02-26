package oyun.setup

final class Env(
  hub: oyun.hub.Env) {

  lazy val forms = new FormFactory()

  lazy val processor = new Processor(
    lobby = hub.actor.lobby)
}

object Env {
  lazy val current = new Env(
    hub = oyun.hub.Env.current
  )
}
