package oyun.api

import play.api.libs.json._

import oyun.common.PimpedJson._
import oyun.game.GameRepo
import oyun.user.{ UserRepo, User, Perfs }

private[api] final class UserApi(
  jsonView: oyun.user.JsonView,
  makeUrl: String => String) {

  def one(username: String)(implicit ctx: Context): Fu[Option[JsObject]] = UserRepo named username flatMap {
    case None => fuccess(none)
    case Some(u) =>
      (GameRepo mostUrgentGame u) map {
        case gameOption =>
          jsonView(u) ++ {
            Json.obj(
              "url" -> makeUrl(s"@/$username"),
              "playing" -> gameOption.map(g => makeUrl(s"${g.gameId}/${g.side.name}")),
              "count" -> Json.obj(
                "all" -> u.count.game,
                "rated" -> u.count.rated
              ))
          }
      } map (_.some)
  }
}
