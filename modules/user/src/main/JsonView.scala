package oyun.user

import oyun.common.PimpedJson._
import play.api.libs.json._

final class JsonView(isOnline: String => Boolean) {

  def apply(u: User) = Json.obj(
    "id" -> u.id,
    "username" -> u.username,
    "online" -> isOnline(u.id),
    "language" -> u.lang,
    "createdAt" -> u.createdAt,
    "seenAt" -> u.seenAt
  ).noNull
}
