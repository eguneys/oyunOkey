package oyun.lobby

import actorApi.{ JoinHook, LobbyUser }
import oyun.game.{ Game, Player }
import okey.{ Side, EastSide, WestSide }

private[lobby] object Biter {

  def apply(hook: Hook, uid: String, user: Option[LobbyUser]): Fu[JoinHook] =
    canJoin(hook, user).fold(
      join(hook, uid, user),
      fufail(s"$user cannot bite hook $hook")
    )

  private def join(hook: Hook, uid: String, lobbyUserOption: Option[LobbyUser]): Fu[JoinHook] = {
    val side = okey.WestSide
    val game = makeGame(hook, side)
    fuccess(JoinHook(uid, hook, game, side))
  }

  private def makeGame(hook: Hook, side: Side) = Game.make(
    players = Player.twoSides(EastSide, side)
  )

  def canJoin(hook: Hook, user: Option[LobbyUser]): Boolean =
    true
}
