package oyun.masa

import okey.Side

case class Player(
  _id: String,
  masaId: String,
  userId: Option[String] = None,
  active: Boolean = false,
  side: Side = Side.EastSide) {
  def id = _id

  def hasUser = userId.isDefined

  def doActiveSide(side: Side) = copy(active = true, side = side)

  def ref = PlayerRef(id = id, userId = userId)
}

object Player {

  case class Active(player: Player)

  private[masa] def make(masaId: String) = new Player(
    _id = oyun.game.IdGenerator.game,
    masaId = masaId
  )
}

case class PlayerRef(
  id: String = oyun.game.IdGenerator.game,
  userId: Option[String] = None) {

  def toPlayer(masaId: String) = Player(
    _id = id,
    masaId = masaId)

}
