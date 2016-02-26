package oyun.app

final class Env() {

}

object Env {
  def setup = oyun.setup.Env.current
  def lobby = oyun.lobby.Env.current
}
