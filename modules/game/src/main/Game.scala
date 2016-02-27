package oyun.game

import okey.{ Sides, Side }

case class Game(
  id: String,
  players: Sides[Option[Player]]) {

  val playerList = players.toList flatten

  def player(side: Side): Option[Player] = players(side)

  def player(playerId: String): Option[Player] =
    playerList find (_.id == playerId)

  def fullIdOf(side: Side): Option[String] = players(side) map (player => s"$id${player.id}")
}


object Game {
  val gameIdSize = 8
  val playerIdSize = 4
  val fullIdSize = 12

  def takeGameId(fullId: String) = fullId take gameIdSize
  def takePlayerId(fullId: String) = fullId drop gameIdSize

  def make(players: Sides[Option[Player]]): Game = Game(
    id = IdGenerator.game,
    players = players
  )

  private[game] lazy val tube = oyun.db.BsTube(BSONHandlers.gameBSONHandler)

  object BSONFields {
    val id = "_id"
    val playerIds = "is"
  }
}
