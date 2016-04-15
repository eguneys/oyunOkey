package oyun.masa

import okey.Side

case class MiniStanding(
  masa: Masa,
  standing: Option[RankedPlayers])

case class PlayerInfo(side: Side, active: Boolean)

case class VisibleMasas(
  created: List[Masa],
  started: List[Masa],
  finished: List[Masa])

case class RankedPlayer(rank: Int, player: Player) {
  override def toString = s"$rank. ${player.id}"
}

object RankedPlayer {
  // def apply(ranking: Ranking)(player: Player): Option[RankedPlayer] =
  //   ranking get player.playerId map { rank =>
  //     RankedPlayer(rank + 1, player)
  //   }
}
