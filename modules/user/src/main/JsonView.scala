package oyun.user

import oyun.common.PimpedJson._
import play.api.libs.json._
import oyun.rating.{ Perf, PerfType }
import User.{ LightPerf }

final class JsonView(isOnline: String => Boolean) {

  import JsonView._

  def apply(u: User) = Json.obj(
    "id" -> u.id,
    "username" -> u.username,
    "online" -> isOnline(u.id),
    "language" -> u.lang,
    "createdAt" -> u.createdAt,
    "seenAt" -> u.seenAt
  ).noNull

  def lightPerfIsOnline(lp: LightPerf) = {
    val json = lightPerfWrites.writes(lp)
    if (isOnline(lp.user.id)) json ++ Json.obj("online" -> true)
    else json
  }
}

object JsonView {

  implicit val lightPerfWrites = OWrites[LightPerf] { l =>
    Json.obj(
      "id" -> l.user.id,
      "username" -> l.user.name,
      "perfs" -> Json.obj(
        l.perfKey -> Json.obj("rating" -> l.rating, "progress" -> l.progress))
    ).noNull
  }


  implicit val perfWrites: OWrites[Perf] = OWrites { o =>
    Json.obj(
      "games" -> o.nb,
      "rating" -> o.gliokey.rating.toInt,
      "prog" -> o.progress)
  }
}
