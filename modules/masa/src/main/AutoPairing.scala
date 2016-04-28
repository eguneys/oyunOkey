package oyun.masa

import oyun.game.{ GameRepo, Game, Player => GamePlayer }

final class AutoPairing {
  def apply(masa: Masa, pairing: Pairing): Fu[Game] = for {
    players <- (pairing.playerIds map getPlayer).sequenceFu
    game1 = Game.make(
      game = okey.Game(okey.variant.Variant.test),
      players = GamePlayer.allSides
    )
    game2 = game1
    .updatePlayers(players.map { p => (gp: GamePlayer) => gp.withPlayer(p.id).withUser(p.userId) })
    .withMasaId(masa.id)
    .withId(pairing.gameId)
    .start
    _ <- (GameRepo insertDenormalized game2)
  } yield game2

  private def getPlayer(playerId: String): Fu[Player] =
    PlayerRepo byId playerId flatMap {
      _.fold(fufail[Player]("No player id " + playerId))(fuccess)
    }
}
