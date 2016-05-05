package oyun.masa

import org.joda.time.{ DateTime }

import okey.Side

case class Player(
  _id: String,
  masaId: String,
  userId: Option[String] = None,
  active: Boolean = false,
  side: Side = Side.EastSide,
  score: Int = 0,
  aiLevel: Option[Int],
  createdAt: DateTime,
  magicScore: Int = 0) {

  def id = _id

  def withdraw = !active

  def is(pid: String): Boolean = pid == id
  def isUser(uid: String): Boolean = uid == userId

  def isRecentlyCreated = (nowSeconds - createdAt.getSeconds) < 60

  def hasUser = userId.isDefined

  def isAi = aiLevel.isDefined

  def doActiveSide(side: Side) = copy(side = side, active = true)

  def ref = PlayerRef(id = id, userId = userId)

  def recomputeMagicScore = copy(magicScore = (active ?? 10000) + (score * -1))
}

object Player {

  case class Active(player: Player)

  private[masa] def make(
    masaId: String,
    aiLevel: Option[Int] = None) = new Player(
    _id = oyun.game.IdGenerator.game,
    masaId = masaId,
    aiLevel = aiLevel,
    createdAt = DateTime.now
  ).recomputeMagicScore
}

case class PlayerRef(
  id: String = oyun.game.IdGenerator.game,
  userId: Option[String] = None,
  aiLevel: Option[Int] = None) {

  def toPlayer(masaId: String) = Player(
    _id = id,
    masaId = masaId,
    userId = userId,
    aiLevel = aiLevel,
    createdAt = DateTime.now)

}
