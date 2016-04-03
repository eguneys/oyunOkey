package oyun.game

import play.api.libs.json._
import oyun.common.PimpedJson._

import okey.{ Move => OkeyMove, _ }

sealed trait Event {
  def typ: String
  def data: JsValue
  def only: Option[Side] = None
  def owner: Boolean = false
  def watcher: Boolean = false
}

object Event {

  object Move {
    def apply(side: Side, move: OkeyMove, situation: Situation, state: State): Move = Move(
      side = side,
      action = move.action,
      drawMiddle = matchDrawMiddle(side, state.side, move),
      discard = matchDiscard(move),
      opens = matchOpens(move),
      drop = matchDrop(move),
      fen = okey.format.Forsyth.exportTable(situation.table, side),
      state = state,
      possibleMoves = situation.actions
    )

    def data(
      fen: String,
      state: State,
      possibleMoves: List[Action])(extra: JsObject) = {
      extra ++ Json.obj(
        "fen" -> fen,
        "ply" -> state.turns,
        "status" -> state.status,
        "dests" -> PossibleMoves.json(possibleMoves)
      ).noNull
    }


    private def matchDrawMiddle(side1: Side, side2: Side, move: OkeyMove): Option[PieceData] = move.action match {
      case DrawMiddle(p) if side1 == side2 => PieceData(p).some
      case _ => None
    }
    private def matchDiscard(move: OkeyMove): Option[PieceData] = move.action match {
      case Discard(p) => PieceData(p).some
      case _ => None
    }

    private def matchOpens(move: OkeyMove): Option[PieceGroupData] = move.action match {
      case OpenSeries(g) => PieceGroupData(g).some
      case OpenPairs(g) => PieceGroupData(g).some
      case _ => None
    }

    private def matchDrop(move: OkeyMove): Option[DropData] = move.action match {
      case DropOpenSeries(piece, pos) => DropData(piece, pos).some
      case DropOpenPairs(piece, pos) => DropData(piece, pos).some
      case _ => None
    }
  }

  case class Move(
    side: Side,
    action: Action,
    drawMiddle: Option[PieceData],
    discard: Option[PieceData],
    opens: Option[PieceGroupData],
    drop: Option[DropData],
    fen: String,
    state: State,
    possibleMoves: List[Action]) extends Event {
    def typ = "move"

    override def only = Some(side)

    def data = Move.data(fen, state, possibleMoves) {
      Json.obj(
        "key" -> action.key,
        "drawmiddle" -> drawMiddle.map(_.data),
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


  case class End(result: Option[Sides[EndScoreSheet]]) extends Event {
    def typ = "end"
    def data = Json.obj("result" ->  result.map(sidesWriter(_)))
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
    status: Option[Status]) extends Event {
    def typ = "state"
    def data = Json.obj(
      "side" -> side,
      "turns" -> turns,
      "status" -> status
    ).noNull
  }

  private def sidesWriter(sides: Sides[okey.EndScoreSheet]) =
    Json.obj(
      "east" -> sides(EastSide),
      "west" -> sides(WestSide),
      "north" -> sides(NorthSide),
      "south" -> sides(SouthSide)
    )


  private implicit val sideWriter: Writes[okey.Side] = Writes { s =>
    JsString(s.name)
  }

  private implicit val statusWriter: OWrites[okey.Status] = OWrites { s =>
    Json.obj(
      "id" -> s.id,
      "name" -> s.name)
  }

  private implicit val endScoreSheetWriter: OWrites[okey.EndScoreSheet] = OWrites { s =>
    Json.obj(
      "hand" -> s.handSum,
      "total" -> s.total,
      "scores" -> JsObject(s.scores map {
        case (k, v) => k.id.toString -> (JsNumber(v.map(_.id) | 0))
      })
    )
  }
}
