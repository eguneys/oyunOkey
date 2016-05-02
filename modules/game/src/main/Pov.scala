package oyun.game

import okey.Side

case class Pov(game: Game, side: Side) {

  def player = game player side

  def playerId = player.id

  def fullId = game fullIdOf side

  def gameId = game.id

  def withSide(s: Side) = copy(side = s)

  def opponentLeft = game player side.previous
  def opponentRight = game player side.next
  def opponentUp = game player side.next.next
}

object Pov {

  def apply(game: Game, player: Player) = new Pov(game, player.side)

  def apply(game: Game, playerId: String): Option[Pov] =
    game player playerId map { apply(game, _) }

  def apply(game: Game, user: oyun.user.User): Option[Pov] =
    game player user map { apply(game, _) }
}

case class PlayerRef(gameId: String, playerId: String)

object PlayerRef {
  def apply(fullId: String): PlayerRef = PlayerRef(Game takeGameId fullId, Game takePlayerId fullId)
}
