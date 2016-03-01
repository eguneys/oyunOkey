package oyun.game

import okey.Side

case class Pov(game: Game, side: Side) {

  def player = game player side

  def playerId = player map (_.id) getOrElse ""

  def gameId = game.id

  def fullId = game fullIdOf side getOrElse ""

  def opponentLeft = game player side.previous
  def opponentRight = game player side.next
  def opponentUp = game player side.next.next
}

object Pov {

  def apply(game: Game, player: Player) = new Pov(game, player.side)
}

case class PlayerRef(gameId: String, playerId: String)

object PlayerRef {
  def apply(fullId: String): PlayerRef = PlayerRef(Game takeGameId fullId, Game takePlayerId fullId)
}
