package oyun.game

import okey.Side

case class Pov(game: Game, side: Side) {

  def gameId = game.id
}

object Pov {

  def apply(game: Game, player: Player) = new Pov(game, player.side)
}

case class PlayerRef(gameId: String, playerId: String)

object PlayerRef {
  def apply(fullId: String): PlayerRef = PlayerRef(Game takeGameId fullId, Game takePlayerId fullId)
}
