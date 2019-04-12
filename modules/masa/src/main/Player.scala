package oyun.masa

import org.joda.time.{ DateTime }

import oyun.rating.Perf
import oyun.user.{ User, Perfs }

import okey.Side

case class Player(
  _id: String,
  masaId: String,
  playerId: String,
  userId: Option[String] = None,
  rating: Option[Int],
  active: Boolean = false,
  side: Side = Side.EastSide,
  score: Int = 0,
  ratingDiff: Int = 0,
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

  def randomPid = oyun.game.IdGenerator.game

  def doSide(side: Side) = copy(side = side)

  def doActiveSide(side: Side) = copy(side = side, active = true)

  def doActivePlayer(player: Player) = copy(playerId = player.playerId,
    userId = player.userId,
    rating = player.rating,
    aiLevel = player.aiLevel,
    active = true)

  def finalRating = rating ?? (ratingDiff+)

  def ref(user: Option[User]) = PlayerRef(playerId = playerId, user = user)

  def recomputeMagicScore = copy(magicScore = (active ?? 10000) + (score * -1))
}

object Player {

  case class Active(player: Player)

  private[masa] def make(
    masaId: String,
    score: Int,
    aiLevel: Option[Int] = None) = new Player(
    _id = oyun.game.IdGenerator.game,
      playerId = oyun.game.IdGenerator.game,
      masaId = masaId,
      aiLevel = aiLevel,
      score = score,
      rating = None,
      createdAt = DateTime.now
  ).recomputeMagicScore

  def randomPid = oyun.game.IdGenerator.game
}

case class PlayerRef(
  active: Boolean = true,
  id: String = oyun.game.IdGenerator.game,
  playerId: String = oyun.game.IdGenerator.game,
  user: Option[User] = None,
  aiLevel: Option[Int] = None) {

  def userId = user map (_.id)

  def toPlayer(masa: Masa, perfLens: Perfs => Perf) = Player(
    _id = id,
    playerId = playerId,
    masaId = masa.id,
    score = masa.scores | 0,
    userId = userId,
    rating = user map { u => perfLens(u.perfs).intRating }, 
    aiLevel = aiLevel,
    active = active,
    createdAt = DateTime.now)

}
