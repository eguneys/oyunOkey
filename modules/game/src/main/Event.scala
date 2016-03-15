package oyun.game

import play.api.libs.json._
import oyun.common.PimpedJson._

import okey.{ Side, Situation, Move => OkeyMove, Status }

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
      fen = okey.format.Forsyth.exportTable(situation.table, side),
      state = state
    )

    def data(fen: String, state: State)(extra: JsObject) = {
      extra ++ Json.obj(
        "fen" -> fen,
        "ply" -> state.turns,
        "status" -> state.status
      ).noNull
    }
  }

  case class Move(
    side: Side,
    fen: String,
    state: State) extends Event {
    def typ = "move"

    override def only = Some(side)

    def data = Move.data(fen, state) {
      Json.obj(
      )
    }
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
