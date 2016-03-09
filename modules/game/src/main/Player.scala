package oyun.game

import okey.{ Side, Sides }

case class Player(
  id: String,
  side: Side) {
}

object Player {

  def make(side: Side): Player = Player(
    id = IdGenerator.player,
    side = side)

  def east = make(Side.EastSide)
  def west = make(Side.WestSide)
  def north = make(Side.NorthSide)
  def south = make(Side.SouthSide)

  def allSides = Sides(east, west, north, south)
}
