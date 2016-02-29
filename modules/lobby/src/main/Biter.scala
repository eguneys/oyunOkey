package oyun.lobby

import actorApi.{ JoinHook, LobbyUser }
import oyun.game.{ GameRepo, Game, Player }
import okey.{ Game => OkeyGame, Table, Player => OkeyPlayer, Side, EastSide, WestSide }

private[lobby] object Biter {

  def apply(hook: Hook, uid: String, user: Option[LobbyUser]): Fu[JoinHook] =
    canJoin(hook, user).fold(
      join(hook, uid, user),
      fufail(s"$user cannot bite hook $hook")
    )

  private def join(hook: Hook, uid: String, lobbyUserOption: Option[LobbyUser]): Fu[JoinHook] = {
    val side = okey.WestSide
    val game = makeGame(hook, side)
    (GameRepo insertDenormalized game) inject {
      JoinHook(uid, hook, game, side)
    }
  }

  private def makeGame(hook: Hook, side: Side) = Game.make(
    game = OkeyGame(
      table = Table init okey.variant.Variant.default,
      player = OkeyPlayer(EastSide)),
    players = Player.twoSides(EastSide, side)
  )

  def canJoin(hook: Hook, user: Option[LobbyUser]): Boolean =
    true
}
