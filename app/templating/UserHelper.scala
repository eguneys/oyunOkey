package oyun.app
package templating

import controllers.routes
import mashup._
import play.twirl.api.Html

import oyun.common.LightUser
import oyun.user.{ User, UserContext }

trait UserHelper { self: I18nHelper with NumberHelper with StringHelper =>

  def showProgress(progress: Int, withTitle: Boolean = true) = Html {
    val span = progress match {
      case 0 => ""
      case p if p > 0 => s"""<span class="positive" data-icon="N">$p</span>"""
      case p if p < 0 => s"""<span class="negative" data-icon="M">${math.abs(p)}</span>"""
    }
    val title = if (withTitle) """data-hint="${trans.ratingProgressionOverTheLastTwelveGames()}"""" else ""
    val klass = if (withTitle) "progress hint--bottom" else "progress"
    s"""<span $title class="$klass">$span</span>"""
  }

  def lightUser(userId: String): Option[LightUser] = Env.user lightUser userId

  def usernameOrAnon(userId: Option[String]) = (userId flatMap(lightUser(_))).fold(User.anonymous)(_.name)

  def isOnline(userId: String) = Env.user isOnline userId

  def userIdLink(
    userIdOption: Option[String],
    withOnline: Boolean = true): Html = Html {
    userIdOption.flatMap(lightUser).fold(User.anonymous) { user =>
      userIdNameLink(
        userId = user.id,
        username = user.name,
        withOnline = withOnline)
    }
  }

  def lightUserLink(
    user: LightUser,
    cssClass: Option[String] = None,
    withOnline: Boolean = true,
    truncate: Option[Int] = None,
    params: String = ""): Html = Html {
    userIdNameLink(
      userId = user.id,
      username = user.name,
      withOnline = withOnline)
  }

  def userIdLink(
    userId: String,
    cssClass: Option[String]): Html = userIdLink(userId.some)

  def userIdNameLink(
    userId: String,
    username: String,
    withOnline: Boolean = true): String = {
    val klass = userClass(userId, withOnline)
    val href = userHref(username)
    val content = username
    val space = if (withOnline) "&nbsp;" else ""
    val dataIcon = if (withOnline) """ data-icon="r"""" else ""
    s"""<a$dataIcon $klass $href>$space$content</a>"""
  }

  def userInfosLink(userId: String) = {
    // val href = userHref(name)

    // Html(s"<a $href>$content</a>""")
    Html(s"User Link")
  }

  private def userHref(username: String) =
    s"""href="${routes.User.show(username)}""""

  protected def userClass(
    userId: String,
    withOnline: Boolean,
    withPowerTip: Boolean = true) = {
    "user_link" :: List(
      withPowerTip option "ulpt",
      withOnline option isOnline(userId).fold("online is-green", "offline")
    ).flatten
  }.mkString("class=\"", " ", "\"")


  def userGameFilterTitle(info: UserInfo, filter: GameFilter)(implicit ctx: UserContext) =
    splitNumber(userGameFilterTitleNoTag(info, filter))

  def userGameFilterTitleNoTag(info: UserInfo, filter: GameFilter)(implicit ctx: UserContext) = Html((filter match {
    case GameFilter.All => info.user.count.game + " " + trans.gamesPlayed()
    case GameFilter.Rated => info.nbRated + " " + trans.rated()
    case GameFilter.Win => trans.nbWins(info.user.count.standing1)
    case GameFilter.Loss => trans.nbWins(info.user.count.standing4)
    case GameFilter.Playing => info.nbPlaying + " " + trans.playing()
  }).toString)

  def describeUser(user: User) = {
    val name = user.titleUsername
    val nbGames = user.count.game
    val createdAt = org.joda.time.format.DateTimeFormat forStyle "M-" print user.createdAt
    val currentRating = 1500
    s"$name played $nbGames games since $createdAt.$currentRating"
  }
}
