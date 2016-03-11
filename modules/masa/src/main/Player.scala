package oyun.masa

import okey.Side

private[masa] case class Player(
  _id: String,
  masaId: String,
  active: Boolean = false,
  side: Side = Side.EastSide) {
  def id = _id
}

private[masa] object Player {
  private[masa] def make(masaId: String) = new Player(
    _id = oyun.game.IdGenerator.game,
    masaId = masaId
  )
}

case class PlayerRef(userId: Option[String] = None) {
  val id = oyun.game.IdGenerator.game

  def toPlayer(masaId: String) = Player(
    _id = id,
    masaId = masaId)
}
