package oyun.masa
package arena

import oyun.masa.{ Score => AbstractScore }
import oyun.masa.{ ScoringSystem => AbstractScoringSystem }

object ScoringSystem extends AbstractScoringSystem {
  case class Score(value: Int) extends AbstractScore

  case class Sheet(oldScore: Int, scores: List[Score]) extends ScoreSheet {
    // val total = scores.foldLeft(0)(_ + _.value)
    val total = (scores.map(_.value).headOption getOrElse 0) + oldScore
  }

  val emptySheet = Sheet(0, Nil)

  def sheet(masa: Masa, playerId: String, pairings: Pairings, oldScore: Int): Sheet = Sheet(oldScore, {
    pairings.foldLeft(List[Score]()) {
      case (scores, p) => Score(p.scoreOf(playerId) | 0) :: scores
    }
  })
}
