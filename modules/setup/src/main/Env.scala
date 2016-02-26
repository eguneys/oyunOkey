package oyun.setup

final class Env() {
  lazy val forms = new FormFactory()
}

object Env {
  lazy val current = new Env(

  )
}
