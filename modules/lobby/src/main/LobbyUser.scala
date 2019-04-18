package oyun.lobby

import oyun.rating.{ PerfType, Perf }
import oyun.user.User

private[lobby] case class LobbyUser(
  id: String, 
  username: String,
  perfMap: LobbyUser.PerfMap) {

  def perfAt(pt: PerfType): LobbyPerf = perfMap.get(pt.key) | LobbyPerf.default

}

private[lobby] object LobbyUser {
  type PerfMap = Map[Perf.Key, LobbyPerf]

  def make(user: User) = LobbyUser(
    id = user.id,
    username = user.username,
    perfMap = perfMapOf(user.perfs))

  private def perfMapOf(perfs: oyun.user.Perfs): PerfMap =
    perfs.perfs.collect {
      case (key, perf) =>
        key -> LobbyPerf(perf.intRating)
    }(scala.collection.breakOut)
}


case class LobbyPerf(rating: Int)

object LobbyPerf {
  val default = LobbyPerf(1500)
}
