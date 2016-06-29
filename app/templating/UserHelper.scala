package oyun.app
package templating

import controllers.routes
import mashup._
import play.twirl.api.Html

import oyun.api.Context
import oyun.common.LightUser
import oyun.rating.{ PerfType, Perf }
import oyun.user.{ User, UserContext, Perfs }

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

  def showPerfRating(rating: Int, name: String, nb: Int, icon: Char, klass: String)(implicit ctx: Context) = Html {
    val title = trans.ratingOverNbGames(name, nb.localize)
    val attr = if (klass == "title") "title" else "data-hint"
    val number = if (nb > 0) rating else "&nbsp;&nbsp;&nbsp;-"
    s"""<span $attr="$title" class="$klass"><span data-icon="$icon">$number</span></span>"""
  }

  def showPerfRating(perfType: PerfType, perf: Perf, klass: String)(implicit ctx: Context): Html =
    showPerfRating(perf.intRating, perfType.name, perf.nb, perfType.iconChar, klass)


  def showPerfRating(u: User, perfType: PerfType, klass: String = "hint--bottom")(implicit ctx: Context): Html =
    showPerfRating(perfType, u perfs perfType, klass)


  def showPerfRating(u: User, perfKey: String)(implicit ctx: Context): Option[Html] =
    PerfType(perfKey) map { showPerfRating(u, _) }

  def showBestPerf(u: User)(implicit ctx: Context): Option[Html] = u.perfs.bestPerf map {
    case (pt, perf) => showPerfRating(pt, perf, klass = "hint--bottom")
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
    cssClass: Option[String] = None,
    withOnline: Boolean = true): String = {
    val klass = userClass(userId, cssClass, withOnline)
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

  def userLink(
    user: User,
    cssClass: Option[String] = None,
    withOnline: Boolean = true,
    withPowerTip: Boolean = true,
    withBestRating: Boolean = false,
    withPerfRating: Option[PerfType] = None,
    text: Option[String] = None,
    params: String = "") = Html {
    val klass = userClass(user.id, cssClass, withOnline, withPowerTip)
    val href = userHref(user.username, params)
    val content = text | user.username
    val space = if (withOnline) "&nbsp;" else ""
    val dataIcon = if (withOnline) """ data-icon="r"""" else ""
    val rating = userRating(user, withPerfRating, withBestRating)
    s"""<a$dataIcon $klass $href>$space$content$rating</a>"""
  }

  private def renderRating(perf: Perf) =
    s"""&nbsp;(${perf.intRating})"""

  private def userRating(user: User, withPerfRating: Option[PerfType], withBestRating: Boolean) =
    withPerfRating match {
      case Some(perfType) => renderRating(user.perfs(perfType))
      case _ if withBestRating => user.perfs.bestPerf ?? {
        case (_, perf) => renderRating(perf)
      }
      case _ => ""
    }

  private def userHref(username: String, params: String = "") =
    s"""href="${routes.User.show(username)}$params""""

  protected def userClass(
    userId: String,
    cssClass: Option[String],
    withOnline: Boolean,
    withPowerTip: Boolean = true) = {
    "user_link" :: List(
      cssClass,
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
