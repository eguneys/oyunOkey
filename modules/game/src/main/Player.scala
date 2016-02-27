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

  def twoSides(side1: Side, side2: Side) =
    Sides[Option[Player]].withSide(side1, Some(make(side1)))
      .withSide(side2, Some(make(side2)))
}
