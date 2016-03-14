package oyun.game

import okey.variant.Variant
import okey.{ Game => OkeyGame, Player => OkeyPlayer, Table, Board, Sides, Side, Opener }

import oyun.db.ByteArray

case class Game(
  id: String,
  players: Sides[Player],
  binaryPieces: Sides[ByteArray],
  binaryDiscards: Sides[ByteArray],
  binaryMiddles: ByteArray,
  binarySign: Int,
  binaryOpens: Option[BinaryOpens],
  binaryPlayer: ByteArray,
  turns: Int,
  variant: Variant = Variant.default,
  metadata: Metadata) {

  val playerList = players.toList

  def player(side: Side): Player = players(side)

  def player(playerId: String): Option[Player] =
    players find (_.id == playerId)

  def playerByPlayerId(playerId: String): Option[Player] = players find (_.playerId == Some(playerId))

  def turnSide = Side(turns)

  def fullIdOf(side: Side): String = s"$id${player(side).id}"


  lazy val toOkey: OkeyGame = {
    val pieces = binaryPieces map BinaryFormat.piece.read

    val discards = binaryDiscards map BinaryFormat.piece.read
    val middles = BinaryFormat.piece.read(binaryMiddles)
    val sign = BinaryFormat.piece.read(binarySign)

    val opener = binaryOpens map { bo =>
      import bo._
      val series = BinaryFormat.opener.readSeries(binarySeries)
      val pairs = BinaryFormat.opener.readPairs(binaryPairs)
      val opens = binaryOpenStates map(_ map BinaryFormat.opener.readState)
      Opener(series, pairs, opens)
    }

    val boards = pieces map Board.apply

    val (playerDrawLeft, playerDrawMiddle) = BinaryFormat.player.read(binaryPlayer)

    val player = OkeyPlayer(
      side = Side(turns),
      drawLeft = playerDrawLeft,
      drawMiddle = playerDrawMiddle)

    OkeyGame(
      table = Table(
        boards = boards,
        discards = discards,
        middles = middles,
        opener = opener,
        sign = sign,
        variant = variant),
      player = player
    )
  }

  def updatePlayers[A](as: Sides[Player => Player]) = copy(
    players = (as zip players) map { case (f, p) => f(p) }
  )

  def withMasaId(id: String) = this.copy(
    metadata = metadata.copy(masaId = id.some)
  )

  def withId(newId: String) = this.copy(id = newId)
}


object Game {
  val gameIdSize = 8
  val playerIdSize = 4
  val fullIdSize = 12

  def takeGameId(fullId: String) = fullId take gameIdSize
  def takePlayerId(fullId: String) = fullId drop gameIdSize

  def make(
    game: OkeyGame,
    players: Sides[Player]): Game = {
    val binaryPieces = game.table.boards map (board => BinaryFormat.piece.write(board.pieceList))
    val binaryDiscards = game.table.discards map BinaryFormat.piece.write
    val binaryMiddles = BinaryFormat.piece write game.table.middles
    val binarySign = BinaryFormat.piece write game.table.sign

    val binaryOpens = game.table.opener map { opener =>
      val binarySeries = BinaryFormat.opener writeSeries opener.series
      val binaryPairs = BinaryFormat.opener writePairs opener.pairs
      val binaryOpenStates = opener.opens map (_ map BinaryFormat.opener.writeState)

      BinaryOpens(binarySeries, binaryPairs, binaryOpenStates)
    }

    val binaryPlayer = BinaryFormat.player write game.player

    Game(
      id = IdGenerator.game,
      players = players,
      binaryPieces = binaryPieces,
      binaryDiscards = binaryDiscards,
      binaryMiddles = binaryMiddles,
      binarySign = binarySign,
      binaryOpens = binaryOpens,
      binaryPlayer = binaryPlayer,
      turns = game.turns,
      metadata = Metadata(
        masaId = none
      )
    )
  }

  private[game] lazy val tube = oyun.db.BsTube(BSONHandlers.gameBSONHandler)

  object BSONFields {
    val id = "_id"
    val playerIds = "is"
    val playerUids = "uis"
    val playerPids = "pis"
    val sidesPlayer = "sip"
    val binaryPieces = "ps"
    val binaryDiscards = "ds"
    val binaryMiddles = "ms"
    val binarySign = "sg"
    val binaryOpens = "opp"
    val binarySeries = "os"
    val binaryPairs = "op"
    val binaryOpenStates = "oo"
    val binaryPlayer = "pl"
    val turns = "t"
    val masaId = "mid"
  }
}

case class BinaryOpens(
  binarySeries: ByteArray,
  binaryPairs: ByteArray,
  binaryOpenStates: Sides[Option[ByteArray]])
