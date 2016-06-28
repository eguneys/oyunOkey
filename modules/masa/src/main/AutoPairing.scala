package oyun.masa

import oyun.game.{ GameRepo, Game, Player => GamePlayer, PerfPicker }

import oyun.user.{ User, UserRepo }

final class AutoPairing {
  def apply(masa: Masa, pairing: Pairing): Fu[Game] = for {
    players <- (pairing.playerIds map getPlayer).sequenceFu
    users <- players.map(_.userId ?? { getUser(_).map(_.some) }).sequenceFu
    game1 = Game.make(
      game = okey.Game(masa.variant) |>{ g =>
        g.copy(
          clock = masa.clock.okeyClock.some
        )
      },
      players = GamePlayer.allSides,
      mode = masa.mode,
      variant = masa.variant)
    game2 = game1
    .updatePlayers(players.map { p => (gp: GamePlayer) =>
      val gp2 = gp.withPlayer(p.id).withAi(p.aiLevel)

      users.flatten.find(u => p.userId.exists(u.id==)).fold(gp2) { user =>
        gp2.withUser(user.id, PerfPicker.mainOrDefault(game1)(user.perfs))
      }
    })
    .withMasaId(masa.id)
    .withRoundAt(masa.nbRounds)
    .withId(pairing.gameId)
    .start
    _ <- (GameRepo insertDenormalized game2)
  } yield game2

  private def getPlayer(playerId: String): Fu[Player] =
    PlayerRepo byId playerId flatMap {
      _.fold(fufail[Player]("No player id " + playerId))(fuccess)
    }

  private def getUser(username: String): Fu[User] =
    UserRepo named username flatMap {
      _.fold(fufail[User]("No user named " + username))(fuccess)
    }
}
