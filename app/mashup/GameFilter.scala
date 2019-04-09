package oyun.app
package mashup

import oyun.game.{ Game }
import oyun.user.User

import scalaz.{ NonEmptyList, IList }

sealed abstract class GameFilter(val name: String)

object GameFilter {
  case object All extends GameFilter("all")
  case object Rated extends GameFilter("rated")
  case object Win extends GameFilter("win")
  case object Loss extends GameFilter("loss")
  case object Playing extends GameFilter("playing")
}

case class GameFilterMenu(all: NonEmptyList[GameFilter],
  current: GameFilter) {

  def list = all.list

}

object GameFilterMenu {

  import GameFilter._

  val all = NonEmptyList.nel(All, IList(Rated, Win, Loss, Playing))

  def apply(
    info: UserInfo,
    me: Option[User],
    currentNameOption: Option[String]): GameFilterMenu = {

    val user = info.user

    val filters: NonEmptyList[GameFilter] = NonEmptyList.nel(All, IList fromList List(
      (info.nbRated > 0) option Rated,
      (info.user.count.standing1 > 0) option Win,
      (info.user.count.standing4 > 0) option Loss,
      (info.nbPlaying > 0) option Playing
    ).flatten)

    val currentName = currentNameOption | Playing.name

    val current = currentOf(filters, currentName)

    new GameFilterMenu(filters, current)
  }

  def currentOf(filters: NonEmptyList[GameFilter], name: String) =
    (filters.list find (_.name == name)) | filters.head
}
