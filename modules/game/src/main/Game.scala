package oyun.game

import org.joda.time.DateTime

import okey.variant.Variant
import okey.{ Game => OkeyGame, Player => OkeyPlayer, History => OkeyHistory, Table, Board, Sides, Side, Opener, Status, Move, EndScoreSheet, Opens, Clock }
import okey.format.Uci

import oyun.db.ByteArray
import oyun.user.User

case class Game(
  id: String,
  players: Sides[Player],
  binaryPieces: Sides[ByteArray],
  binaryDiscards: Sides[ByteArray],
  binaryMiddles: ByteArray,
  binarySign: Int,
  binaryOpens: Option[BinaryOpens],
  binaryPlayer: ByteArray,
  clock: Option[Clock],
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

  def player(user: User): Option[Player] =
    players find (_ isUser user)


  def playerByPlayerId(playerId: String): Option[Player] = players find (_.playerId == Some(playerId))

  def playerByUserId(userId: String): Option[Player] = players find (_.userId == Some(userId))

  def player: Player = player(turnSide)

  def turnSide = Side(turns)

  def turnOf(p: Player): Boolean = p == player
  def turnOf(s: Side): Boolean = s == turnSide

  def playedTurns = turns

  def fullIdOf(side: Side): String = s"$id${player(side).id}"

  def masaId = metadata.masaId

  def hasChat = true

  def updatedAtOrCreatedAt = updatedAt | createdAt

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
      clock = clock,
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
        lastMoves = history.lastMoves,
        turn = turns),
      turns = game.turns,
      status = situation.status | status,
      clock = game.clock
    )

    val state = Event.State(
      side = situation.player.side,
      turns = game.turns,
      status = (status != updated.status) option updated.status
    )

    val clockEvent = updated.clock map Event.Clock.apply

    val events = Event.Move(Side.EastSide, move, situation, state, clockEvent) ::
    Event.Move(Side.WestSide, move, situation, state, clockEvent) ::
    Event.Move(Side.NorthSide, move, situation, state, clockEvent) ::
    Event.Move(Side.SouthSide, move, situation, state, clockEvent) :: Nil

    Progress(this, updated, events)
  }

  def updatePlayers[A](as: Sides[Player => Player]) = copy(
    players = (as zip players) map { case (f, p) => f(p) }
  )


  def start = started.fold(this, copy(
    status = Status.Started,
    updatedAt = DateTime.now.some
  ))

  def finish(
    status: Status,
    result: Option[Sides[EndScoreSheet]],
    winner: Option[Side]) = Progress(
    this,
    copy(
      status = status,
      players = players sideMap ((side, p) => p finish (
        score = result map(_.apply(side)),
        winner = winner == Some(side)))
    ),
    List(Event.End(result))
  )

  def started = status >= Status.Started

  def aborted = status == Status.Aborted

  def playable = status < Status.Aborted

  def playableBy(p: Player): Boolean = playable && turnOf(p)

  def playableBy(s: Side): Boolean = playableBy(player(s))

  def playableByAi: Boolean = playable && player.isAi

  def aiLevel: Option[Int] = players find (_.isAi) flatMap (_.aiLevel)

  def finished = status >= Status.NormalEnd

  def finishedOrAborted = finished || aborted

  def endScores: Option[Sides[EndScoreSheet]] = players.map(_.endScore).toList.sequence.map (Sides.fromIterable)

  def winner = players find (_.wins)

  def winnerSide: Option[Side] = winner map (_.side)

  def outoftime: Boolean = outoftimeClock

  private def outoftimeClock: Boolean = clock ?? { c =>
    started && playable && {
      (!c.isRunning) || c.outoftime(player.side)
    }
  }

  def onePlayerHasMoved = playedTurns > 0
  def allPlayersHaveMoved = playedTurns > 3

  def isBeingPlayed = !finishedOrAborted

  def unplayed = !allPlayersHaveMoved && (createdAt isBefore Game.unplayedDate)

  def abandoned = (status <= Status.Started) && ((updatedAt | createdAt) isBefore Game.abandonedDate)

  def userIds = playerMaps(_.userId)

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

  val unplayedHours = 2
  def unplayedDate = DateTime.now minusMinutes unplayedHours

  val abandonedDays = 24
  def abandonedDate = DateTime.now minusSeconds abandonedDays

  def takeGameId(fullId: String) = fullId take gameIdSize
  def takePlayerId(fullId: String) = fullId drop gameIdSize

  def make(
    game: OkeyGame,
    players: Sides[Player],
    variant: Variant): Game = {
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
      clock = game.clock,
      variant = variant,
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
    val playingUids = "plis"
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
    val clock = "c"
    val variant = "v"
    val winnerSide = "w"
    val winnerId = "wid"
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
  turn: Int,
  lastMoves: List[Uci],
  opens: Sides[Option[Opens]])

object OpensLastMove {
  def init = OpensLastMove(lastMoves = Nil, turn = 0, opens = Sides[Option[Opens]])

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

      val turn = r.get[Int]("t")

      OpensLastMove(
        turn = turn,
        lastMoves = lastMoves,
        opens = opens)
    }

    def writes(w: BSON.Writer, o: OpensLastMove) = BSONDocument(
      "t" -> o.turn,
      "lm" -> Uci.writeList(o.lastMoves),
      "op" -> o.opens
    )
  }
}
