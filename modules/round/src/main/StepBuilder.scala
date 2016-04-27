package oyun.round

import play.api.libs.json._

import okey.variant.Variant
import okey.Side

object StepBuilder {

  private val logger = oyun.round.logger.branch("StepBuilder")

  def apply(
    id: String,
    ply: Int,
    pgnMoves: List[String],
    side: Side,
    variant: Variant): JsArray = {

    val lastStep = Step(
      ply = ply,
      side = side,
      moves = pgnMoves.map(Step.Move.apply))

    val steps = List(lastStep)

    JsArray(steps.map(_.toJson))
  }
}

case class Step(
  ply: Int,
  side: Side,
  moves: List[Step.Move]) {


  def toJson = Step.stepJsonWriter writes this
}

object Step {

  case class Move(uci: String)

  implicit val stepJsonWriter: Writes[Step] =  Writes { step =>
    import step._
    Json.obj(
      "ply" -> ply,
      "side" -> side.name,
      "moves" -> moves.map(m => Json.obj(
        "san" -> m.uci
      )))
  }
}
