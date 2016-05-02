package oyun.round

import oyun.game.{ Game, Progress, Pov, GameRepo }
import ornicar.scalalib.Zero

private final class GameProxy(id: String) {

  val enabled = true

  def game: Fu[Option[Game]] = if (enabled) cache else fetch

  def save(progress: Progress): Funit = {
    set(progress.game)
    GameRepo save progress
  }

  def set(game: Game): Unit = {
    if (enabled) cache = fuccess(game.some)
  }

  def invalidate: Unit = {
    if (enabled) cache = fetch
  }

  // convenience helpers

  def playerPov(playerId: String) = game.map {
    _ flatMap { Pov(_, playerId) }
  }

  def withGame[A: Zero](f: Game => Fu[A]): Fu[A] = game.flatMap(_ ?? f)

  // internals

  private var cache: Fu[Option[Game]] = fetch

  private def fetch = GameRepo game id
}
