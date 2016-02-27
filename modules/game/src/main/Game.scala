package oyun.game

import okey.{ Sides, Side }

case class Game(
  id: String,
  players: Sides[Option[Player]]) {

  def player(side: Side): Option[Player] = players(side)

  def fullIdOf(side: Side): Option[String] = players(side) map (player => s"$id${player.id}")
}


object Game {
  val gameIdSize = 8
  val playerIdSize = 4
  val fullIdSize = 12

  def make(players: Sides[Option[Player]]): Game = Game(
    id = IdGenerator.game,
    players = players
  )
}
