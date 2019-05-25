package oyun.game

import play.api.libs.json._
import oyun.common.PimpedJson._

import okey.{ Move => OkeyMove, Side, Sides, Status, Situation, Action, EndScoreSheet, Piece, PieceGroups, OpenPos, Clock => OkeyClock }

import oyun.chat.{ Line, UserLine, PlayerLine }
import oyun.common.Maths.truncateAt

sealed trait Event {
  def typ: String
  def data: JsValue
  def only: Option[Side] = None
  def owner: Boolean = false
  def watcher: Boolean = false
  def troll: Boolean = false
}

object Event {

  import GameJsonView._

  object Move {
    def apply(side: Side, move: OkeyMove, situation: Situation, state: State, clock: Option[Event]): Move = Move(
      side = side,
      action = move.action,
      drawMiddle = matchDrawMiddle(side, state.side, move),
      discard = matchDiscard(move),
      leaveTaken = matchLeaveTaken(move),
      opens = matchOpens(move),
      drop = matchDrop(move),
      fen = okey.format.Forsyth.exportTable(situation.table, side),
      state = state,
      clock = clock,
      possibleMoves = situation.actions
    )

    def watcher(side: Side, move: OkeyMove, situation: Situation, state: State, clock: Option[Event]): WatcherMove = Move(
      side = side,
      action = move.action,
      drawMiddle = none,
      discard = matchDiscard(move),
      leaveTaken = matchLeaveTaken(move),
      opens = matchOpens(move),
      drop = matchDrop(move),
      fen = okey.format.Forsyth.exportTableWatcher(situation.table, side),
      state = state,
      clock = clock,
      possibleMoves = situation.actions
    ).forWatcher

    def data(
      fen: String,
      state: State,
      clock: Option[Event],
      possibleMoves: List[Action])(extra: JsObject) = {
      extra ++ Json.obj(
        "fen" -> fen,
        "ply" -> state.turns,
        "status" -> state.status,
        "oscores" -> state.opens.map(sidesWriter(_)),
        "dests" -> PossibleMoves.json(possibleMoves),
        "clock" -> clock.map(_.data)
      ).noNull
    }

    def hideAction(cond: Boolean, action: Action) = action match {
      case okey.DrawMiddle(p) if cond => okey.DrawMiddle
      case x => x
    }

    private def matchDrawMiddle(side1: Side, side2: Side, move: OkeyMove): Option[PieceData] = move.action match {
      case okey.DrawMiddle(p) if side1 == side2 => PieceData(p).some
      case _ => None
    }
    private def matchDiscard(move: OkeyMove): Option[PieceData] = move.action match {
      case okey.Discard(p) => PieceData(p).some
      case _ => None
    }

    private def matchLeaveTaken(move: OkeyMove): Option[PieceData] = move.action match {
      case okey.LeaveTaken(p) => PieceData(p).some
      case _ => None
    }

    private def matchOpens(move: OkeyMove): Option[PieceGroupData] = move.action match {
      case okey.OpenSeries(g) => PieceGroupData(g).some
      case okey.OpenPairs(g) => PieceGroupData(g).some
      case _ => None
    }

    private def matchDrop(move: OkeyMove): Option[DropData] = move.action match {
      case okey.DropOpenSeries(piece, pos) => DropData(piece, pos).some
      case okey.DropOpenPairs(piece, pos) => DropData(piece, pos).some
      case _ => None
    }
  }

  case class PlayerMove(side: Side, move: Move) extends Event {
    def typ = "move"
    override def only = Some(side)
    override def owner = true

    def data = move.data
  }

  case class WatcherMove(move: Move) extends Event {
    def typ = "move"
    override def watcher = true

    def data = (move.data ++ Json.obj(
      "uci" -> Move.hideAction(true, move.action).toUci.uci,
      "drawmiddle" -> JsNull
    )).noNull
  }

  case class Move(
    side: Side,
    action: Action,
    drawMiddle: Option[PieceData],
    leaveTaken: Option[PieceData],
    discard: Option[PieceData],
    opens: Option[PieceGroupData],
    drop: Option[DropData],
    fen: String,
    state: State,
    clock: Option[Event],
    possibleMoves: List[Action]) extends Event {

    def forPlayer = PlayerMove(side, this)
    def forWatcher = WatcherMove(this)

    def typ = "move"

    def data = Move.data(fen, state, clock, possibleMoves) {
      Json.obj(
        "key" -> action.key,
        "uci" -> Move.hideAction(side != state.side, action).toUci.uci,
        "drawmiddle" -> drawMiddle.map(_.data),
        "leavetaken" -> leaveTaken.map(_.data),
        "discard" -> discard.map(_.data),
        "opens" -> opens.map(_.data),
        "drop" -> drop.map(_.data)
      )
    }
  }

  object PossibleMoves {
    def json(moves: List[Action]) =
      if (moves.isEmpty) JsNull
      else JsArray(moves.map(move => JsString(move.key)))
  }

  case class PlayerMessage(line: PlayerLine) extends Event {
    def typ = "message"
    def data = Line toJson line
    override def owner = true
    override def troll = false
  }

  case class UserMessage(line: UserLine, w: Boolean) extends Event {
    def typ = "message"
    def data = Line toJson line
    override def owner = w
    override def troll = !w
  }

  case class End(result: Option[Sides[EndScoreSheet]]) extends Event {
    def typ = "end"
    def data = Json.obj("result" ->  result.map(sidesWriter(_)))
  }

  case class EndData(game: Game) extends Event {
    def typ = "endData"

    def data = Json.obj(
      "winner" -> game.winner.flatMap(_.name),
      "status" -> game.status
    )
  }

  case class Clock(times: Sides[Float]) extends Event {
    def typ = "clock"
    def data = sidesWriter(times.map(d => JsNumber(truncateAt(d, 2))))
  }

  object Clock {
    def apply(clock: OkeyClock): Clock = Clock(Sides(clock.remainingTime(_)))
  }

  case class PieceGroupData(groups: PieceGroups) extends Event {
    def typ = "piecegroupdata"
    def data = Json.obj(
      "group" -> okey.format.Forsyth.exportGroups(groups)
    )
  }

  case class PieceData(piece: Piece) extends Event {
    def typ = "piecedata"
    def data = Json.obj(
      "piece" -> piece.key
    )
  }

  case class DropData(piece: Piece, pos: OpenPos) extends Event {
    def typ = "dropdata"
    def data = Json.obj(
      "piece" -> piece.key,
      "pos" -> pos.toString
    )
  }

  case class State(
    side: Side,
    turns: Int,
    status: Option[Status],
    opens: Option[Sides[Option[okey.OpenState]]]) extends Event {
    def typ = "state"
    def data = Json.obj(
      "side" -> side,
      "turns" -> turns,
      "status" -> status,
      "oscores" -> opens.map(sidesWriter(_))
    ).noNull
  }

  case class Crowd(
    sidesOnGame: Sides[Boolean]
  ) extends Event {
    def typ = "crowd"
    def data = sidesWriter(sidesOnGame) ++ Json.obj(
      "watchers" -> 0
    )
  }
}

object GameJsonView {

  def sidesWriter[A](sides: Sides[A])(implicit writer: Writes[A]) =
    Json.obj(
      "east" -> sides(Side.EastSide),
      "west" -> sides(Side.WestSide),
      "north" -> sides(Side.NorthSide),
      "south" -> sides(Side.SouthSide)
    )


  implicit val sideWriter: Writes[okey.Side] = Writes { s =>
    JsString(s.name)
  }

  implicit val statusWriter: OWrites[okey.Status] = OWrites { s =>
    Json.obj(
      "id" -> s.id,
      "name" -> s.name)
  }

  implicit val endScoreSheetWriter: OWrites[okey.EndScoreSheet] = OWrites { s =>
    Json.obj(
      "hand" -> s.handSum,
      "total" -> s.total,
      "scores" -> JsObject(s.scores map {
        case (k, v) => k.id.toString -> (JsNumber(v.map(_.id) | 0))
      })
    )
  }  

  implicit val openStateWriter: OWrites[okey.OpenState] = OWrites {
    case okey.OldOpen(score) => openScoreWriter(score)
    case okey.NewOpen(score, _, _) => openScoreWriter(score, true)
  }

  def openScoreWriter(score: okey.OpenScore, isNew: Boolean = false): JsObject = (score match {
    case okey.SerieScore(score) => Json.obj("series" -> score)
    case okey.PairScore(score) => Json.obj("pairs" -> score)
  }) ++ Json.obj("new" -> isNew.option(true))
}
