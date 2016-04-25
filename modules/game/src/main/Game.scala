package oyun.game

import org.joda.time.DateTime

import okey.variant.Variant
import okey.{ Game => OkeyGame, Player => OkeyPlayer, History => OkeyHistory, Table, Board, Sides, Side, Opener, Status, Move, EndScoreSheet, Opens }
import okey.format.Uci

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
  opensLastMove: OpensLastMove,
  status: Status,
  turns: Int,
  variant: Variant = Variant.default,
  createdAt: DateTime = DateTime.now,
  updatedAt: Option[DateTime] = None,
  metadata: Metadata) {

  val playerList = players.toList

  def player(side: Side): Player = players(side)

  def player(playerId: String): Option[Player] =
    players find (_.id == playerId)

  def playerByPlayerId(playerId: String): Option[Player] = players find (_.playerId == Some(playerId))

  def playerByUserId(userId: String): Option[Player] = players find (_.userId == Some(userId))

  def player: Player = player(turnSide)

  def turnSide = Side(turns)

  def turnOf(p: Player): Boolean = p == player
  def turnOf(s: Side): Boolean = s == turnSide

  def fullIdOf(side: Side): String = s"$id${player(side).id}"

  def masaId = metadata.masaId

  lazy val toOkey: OkeyGame = {
    val pieces = binaryPieces map BinaryFormat.piece.read

    val discards = binaryDiscards map BinaryFormat.piece.read
    val middles = BinaryFormat.piece.read(binaryMiddles)
    val sign = BinaryFormat.piece.read(binarySign)

    val opener = binaryOpens map { bo =>
      import bo._

      val save = bo.save map { case (savePieces, saveOpens) =>

        val board = Board(BinaryFormat.piece.read(savePieces))

        val series = BinaryFormat.opener.readSeries(saveOpens.binarySeries)
        val pairs = BinaryFormat.opener.readPairs(saveOpens.binaryPairs)
        val opens = saveOpens.binaryOpenStates map (_ map BinaryFormat.opener.readState(None))

        (board, Opener(series, pairs, opens))
      }

      val series = BinaryFormat.opener.readSeries(binarySeries)
      val pairs = BinaryFormat.opener.readPairs(binaryPairs)
      val opens = binaryOpenStates map(_ map BinaryFormat.opener.readState(save))

      Opener(series, pairs, opens)
    }

    val boards = pieces map Board.apply

    val (playerDrawLeft, playerDrawMiddle) = BinaryFormat.player.read(binaryPlayer)

    val player = OkeyPlayer(
      side = Side(turns),
      history = toOkeyHistory,
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
      player = player,
      turns = turns
    )
  }

  lazy val toOkeyHistory = OkeyHistory(
    lastMoves = opensLastMove.lastMoves,
    openStates = opensLastMove.opens)

  def update(
    game: OkeyGame,
    move: Move): Progress = {
    val (history, situation) = (game.player.history, game.situation)

    val bOpens = game.table.opener map { opener =>

      val save = opener.getSave map {
        case (board, opener) =>
          val pieces = BinaryFormat.piece.write(board.pieceList)

          val bo = BinaryOpens(
            binarySeries = BinaryFormat.opener writeSeries opener.series,
            binaryPairs = BinaryFormat.opener writePairs opener.pairs,
            binaryOpenStates = opener.opens.map(_ map BinaryFormat.opener.writeState))

          (pieces, bo)
      }

      BinaryOpens(
        binarySeries = BinaryFormat.opener writeSeries opener.series,
        binaryPairs = BinaryFormat.opener writePairs opener.pairs,
        binaryOpenStates = opener.opens.map(_ map BinaryFormat.opener.writeState),
        save = save
      )
    }

    val updated = copy(
      binaryPieces = game.table.boards map (board => BinaryFormat.piece.write(board.pieceList)),
      binaryDiscards = game.table.discards map BinaryFormat.piece.write,
      binaryMiddles = BinaryFormat.piece write game.table.middles,
      binaryOpens = bOpens,
      binaryPlayer = BinaryFormat.player write game.player,
      opensLastMove = OpensLastMove(
        opens = history.openStates,
        lastMoves = history.lastMoves),
      turns = game.turns,
      status = situation.status | status
    )

    val state = Event.State(
      side = situation.player.side,
      turns = game.turns,
      status = (status != updated.status) option updated.status
    )

    val events = Event.Move(Side.EastSide, move, situation, state) ::
    Event.Move(Side.WestSide, move, situation, state) ::
    Event.Move(Side.NorthSide, move, situation, state) ::
    Event.Move(Side.SouthSide, move, situation, state) :: Nil

    Progress(this, updated, events)
  }

  def updatePlayers[A](as: Sides[Player => Player]) = copy(
    players = (as zip players) map { case (f, p) => f(p) }
  )


  def start = started.fold(this, copy(
    status = Status.Started,
    updatedAt = DateTime.now.some
  ))

  def finish(status: Status, result: Option[Sides[EndScoreSheet]]) = Progress(
    this,
    copy(
      status = status,
      players = players sideMap ((side, p) => p finish (result map(_.apply(side))))
    ),
    List(Event.End(result))
  )

  def started = status >= Status.Started

  def aborted = status == Status.Aborted

  def playable = status < Status.Aborted

  def playableBy(p: Player): Boolean = playable && turnOf(p)

  def playableBy(s: Side): Boolean = playableBy(player(s))

  def finished = status >= Status.End

  def finishedOrAborted = finished || aborted

  def endScores: Option[Sides[EndScoreSheet]] = players.map(_.endScore).toList.sequence.map (Sides.fromIterable)

  def isBeingPlayed = !finishedOrAborted

  def playerIds = playerMaps(_.playerId)

  def withMasaId(id: String) = this.copy(
    metadata = metadata.copy(masaId = id.some)
  )

  def withId(newId: String) = this.copy(id = newId)

  private def playerMaps[A](f: Player => Option[A]): List[A] = players flatMap { f(_) } toList
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
      opensLastMove = OpensLastMove.init,
      status = Status.Created,
      turns = game.turns,
      metadata = Metadata(
        masaId = none
      ),
      createdAt= DateTime.now)
  }

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
    val binaryOpensSave = "oppss"
    val binaryPiecesSave = "psss"
    val binarySeries = "os"
    val binaryPairs = "op"
    val binaryOpenStates = "oo"
    val binaryPlayer = "pl"
    val opensLastMove = "ol"
    val status = "s"
    val turns = "t"
    val endScores = "es"
    val createdAt = "ca"
    val updatedAt = "ua"
    val masaId = "mid"
    val checkAt = "ck"
  }
}

case class BinaryOpens(
  binarySeries: ByteArray,
  binaryPairs: ByteArray,
  binaryOpenStates: Sides[Option[ByteArray]],
  save: Option[(ByteArray, BinaryOpens)] = None)

case class OpensLastMove(
  lastMoves: List[Uci],
  opens: Sides[Option[Opens]])

object OpensLastMove {
  def init = OpensLastMove(lastMoves = Nil, opens = Sides[Option[Opens]])

  import reactivemongo.bson._
  import oyun.db.BSON
  import BSONHandlers.sidesOptionBSONHandler

  private[game] implicit val opensBSONHandler = new BSON[Opens] {
    def reads(r: BSON.Reader) = Opens(old = r bool "o", pairs = r bool "p")
    def writes(w: BSON.Writer, o: Opens) = BSONDocument(
      "o" -> o.old,
      "p" -> o.pairs)
  }

  private[game] implicit val opensLastMoveBSONHandler = new BSON[OpensLastMove] {
    def reads(r: BSON.Reader) = {
      val lastMoves = (r str "lm") |> {
        case "" => Nil
        case s => Uci.readList(s) err s"invalid last moves: $s"
      }

      val opens = r.get[Sides[Option[Opens]]]("op")

      OpensLastMove(lastMoves, opens)
    }

    def writes(w: BSON.Writer, o: OpensLastMove) = BSONDocument(
      "lm" -> Uci.writeList(o.lastMoves),
      "op" -> o.opens
    )
  }
}
