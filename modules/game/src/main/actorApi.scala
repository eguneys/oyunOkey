package oyun.game
package actorApi

import oyun.user.User

case class FinishGame(game: Game, users: okey.Sides[Option[User]])

case class WithdrawMasa(masaId: String, playerId: String)

case class AbortCurrentGame(gameId: String)
