package oyun.game

import okey.{ Side, Sides, EndScoreSheet }

import oyun.user.User

case class Player(
  id: String,
  seatId: Option[String] = None,
  playerId: Option[String] = None,
  userId: Option[String] = None,
  side: Side,
  aiLevel: Option[Int],
  rating: Option[Int] = None,
  isWinner: Option[Boolean] = None,
  endScore: Option[EndScoreSheet] = None,
  standing: Option[Int] = None,
  name: Option[String] = None) {

  def finish(score: Option[EndScoreSheet], standing: Option[Int], winner: Boolean) = copy(
    endScore = endScore,
    standing = standing,
    isWinner = if (winner) Some(true) else None
  )

  def withSeat(seatId: String): Player = copy(
    seatId = seatId.some)

  def withPlayer(id: String): Player = copy(
    playerId = id.some)

  def withUser(id: String, perf: oyun.rating.Perf): Player = copy(
    userId = id.some,
    rating = perf.intRating.some)

  def withAi(aiLevel: Option[Int]): Player = copy(
    aiLevel = aiLevel)

  def hasUser = userId.isDefined

  def isAi = aiLevel.isDefined

  def isUser(u: User) = userId.fold(false)(_ == u.id)

  def wins = isWinner getOrElse false
}

object Player {

  type ID = String

  def make(
    side: Side,
    aiLevel: Option[Int] = None): Player = Player(
      id = IdGenerator.player,
      side = side,
      aiLevel = aiLevel)

  def east = make(Side.EastSide)
  def west = make(Side.WestSide)
  def north = make(Side.NorthSide)
  def south = make(Side.SouthSide)

  def allSides = Sides(east, west, north, south)


  object BSONFields {
    val aiLevel = "ai"
    val rating = "e"
    val name = "n"
  }


  import reactivemongo.bson._
  import oyun.db.BSON

  type Id = String
  type UserId = Option[String]
  type SeatId = Option[String]
  type PlayerId = Option[String]
  type EndScore = Option[EndScoreSheet]
  type EndStanding = Option[Int]
  type Win = Option[Boolean]

  type Builder = Side => Id => PlayerId => UserId => SeatId => EndScore => EndStanding => Win => Player

  implicit val playerBSONHandler = new BSON[Builder] {
    import BSONFields._

    def reads(r: BSON.Reader) = side => id => playerId => userId => seatId => endScore => standing => win => Player(
      id = id,
      side = side,
      playerId = playerId,
      userId = userId,
      seatId = seatId,
      rating = r intO rating,
      aiLevel = r intO aiLevel,
      endScore = endScore,
      standing = standing,
      isWinner = win,
      name = r strO name)

    def writes(w: BSON.Writer, o: Builder) =
      o(Side.EastSide)("0000")(none)(none)(none)(none)(none)(none) |> { p =>
        BSONDocument(
          aiLevel -> p.aiLevel,
          rating -> p.rating,
          name -> p.name
        )
      }
  }
}
