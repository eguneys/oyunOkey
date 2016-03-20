package oyun.game

import play.api.libs.json._
import oyun.common.PimpedJson._

import okey.{ Side, Situation, Piece, Move => OkeyMove, DrawMiddle, Discard, Status, Action }

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
  }

  case class Move(
    side: Side,
    action: Action,
    drawMiddle: Option[PieceData],
    discard: Option[PieceData],
    fen: String,
    state: State,
    possibleMoves: List[Action]) extends Event {
    def typ = "move"

    override def only = Some(side)

    def data = Move.data(fen, state, possibleMoves) {
      Json.obj(
        "key" -> action.key,
        "drawmiddle" -> drawMiddle.map(_.data),
        "discard" -> discard.map(_.data)
      )
    }
  }

  object PossibleMoves {
    def json(moves: List[Action]) =
      if (moves.isEmpty) JsNull
      else JsArray(moves.map(move => JsString(move.key)))
  }

  case class PieceData(piece: Piece) extends Event {
    def typ = "piecedata"
    def data = Json.obj(
      "piece" -> piece.key
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

  private implicit val sideWriter: Writes[okey.Side] = Writes { s =>
    JsString(s.name)
  }

  private implicit val statusWriter: OWrites[okey.Status] = OWrites { s =>
    Json.obj(
      "id" -> s.id,
      "name" -> s.name)
  }
}
