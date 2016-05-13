package oyun.app
package mashup

import oyun.api.Context
import oyun.user.{ User }

case class UserInfo(
  user: User,
  nbPlaying: Int) {

  def nbRated = user.count.rated
}

object UserInfo {

  def apply(
    countUsers: () => Fu[Int],
    gameCached: oyun.game.Cached)(user: User, ctx: Context): Fu[UserInfo] =
    (gameCached nbPlaying user.id) map {
      case (nbPlaying) => new UserInfo(
      user = user,
      nbPlaying = nbPlaying)
    }
}
