package oyun.masa
package arena

import oyun.masa.{ Score => AbstractScore }
import oyun.masa.{ ScoringSystem => AbstractScoringSystem }

object ScoringSystem extends AbstractScoringSystem {
  case class Score(value: Int) extends AbstractScore

  case class Sheet(scores: List[Score]) extends ScoreSheet {
    val total = scores.foldLeft(0)(_ + _.value)
    // val total = (scores.map(_.value).headOption getOrElse 0) + oldScore
    val latest = scores.map(_.value).headOption
  }

  val emptySheet = Sheet(Nil)

  def sheet(masa: Masa, seatId: String, pairings: Pairings): Sheet = Sheet {
    pairings.foldLeft(List[Score]()) {
      case (scores, p) => Score(p.scoreOf(seatId) | 0) :: scores
    }
  }
}
