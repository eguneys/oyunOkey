package oyun.lobby

import scala.concurrent.duration._

import oyun.db.dsl._
import oyun.user.User

final class SeekApi(
  coll: Coll,
  asyncCache: oyun.memo.AsyncCache.Builder,
  maxPerPage: Int,
  maxPerUser: Int
) {


  private sealed trait CacheKey
  private object ForAnon extends CacheKey
  private object ForUser extends CacheKey

  private def allCursor =
    coll.find($empty)
      .sort($doc("createdAt" -> -1))
      .cursor[Seek]()

  private val cache = asyncCache.clearable[CacheKey, List[Seek]](
    name = "lobby.seek.list",
    f = {
      case ForAnon => allCursor.gather[List](maxPerPage)
      case ForUser => allCursor.gather[List]()
    },
    maxCapacity = 2,
    expireAfter = _.ExpireAfterWrite(3.seconds)
  )

  def forAnon = cache get ForAnon

  def forUser(user: User): Fu[List[Seek]] =
    forUser(LobbyUser.make(user))

  def forUser(user: LobbyUser): Fu[List[Seek]] =
    cache get ForUser map { seeks =>
      val filtered = seeks.filter { seek =>
        seek.user.id == user.id || Biter.canJoin(seek, user)
      }
      noDupsFor(user, filtered) take maxPerPage
    }

  private def noDupsFor(user: LobbyUser, seeks: List[Seek]) =
    seeks.foldLeft(List[Seek]() -> Set[String]()) {
      case ((res, h), seek) if seek.user.id == user.id => (seek :: res, h)
      case ((res, h), seek) =>
        val seekH = List(seek.variant, seek.mode, seek.side, seek.user.id) mkString ","
        if (h contains seekH) (res, h)
        else (seek :: res, h + seekH)
    }._1.reverse
  
}
