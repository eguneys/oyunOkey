package oyun.masa

sealed abstract class System(val id: Int) {
  val scoringSystem: ScoringSystem
}

object System {
  case object Arena extends System(id = 1) {
    val scoringSystem = arena.ScoringSystem
  }

  val default = Arena
}

trait Score {
  val value: Int
}

trait ScoreSheet {
  def scores: List[Score]
  def total: Int
}

trait ScoringSystem {
  type Sheet <: ScoreSheet

  def emptySheet: Sheet

  def sheet(masa: Masa, playerId: String, pairings: Pairings, oldScore: Int): Sheet
}
