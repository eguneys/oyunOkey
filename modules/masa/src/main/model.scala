package oyun.masa

import okey.Side

case class MiniStanding(
  masa: Masa,
  standing: Option[RankedPlayers])

case class MasaTop(value: List[Player]) extends AnyVal

case class MasaMiniView(masa: Masa, top: Option[MasaTop])

case class PlayerInfo(seatId: String, side: Side, active: Boolean)

case class MyInfo(seatId: String, side: Side, active: Boolean, gameId: Option[oyun.game.Game.ID])


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

case class FeaturedGame(
  game: oyun.game.Game)
