package oyun.game

import okey.{ Side, Sides, EndScoreSheet }

case class Player(
  id: String,
  playerId: Option[String] = None,
  side: Side,
  endScore: Option[EndScoreSheet] = None) {

  def finish(score: Option[EndScoreSheet]) = copy(
    endScore = endScore
  )

  def withPlayer(id: String): Player = copy(
    playerId = id.some)

  def hasUser = false
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


  object BSONFields {

  }


  import reactivemongo.bson._
  import oyun.db.BSON

  type Id = String
  type UserId = Option[String]
  type PlayerId = Option[String]
  type EndScore = Option[EndScoreSheet]

  type Builder = Side => Id => PlayerId => EndScore => Player

  implicit val playerBSONHandler = new BSON[Builder] {
    import BSONFields._

    def reads(r: BSON.Reader) = side => id => playerId => endScore => Player(
      id = id,
      side = side,
      playerId = playerId,
      endScore = endScore
    )

    def writes(w: BSON.Writer, o: Builder) =
      o(Side.EastSide)("0000")(none)(none) |> { p =>
        BSONDocument(
          
        )
      }
  }
}
