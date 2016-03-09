package oyun.masa

import play.api.libs.json._

final class JsonView() {
  def apply(masa: Masa,
    me: Option[String],
    socketVersion: Option[Int]): Fu[JsObject] = for {
    i <- fuccess(10)
  } yield Json.obj(
    "id" -> masa.id,
    "fullName" -> masa.fullName,
    "socketVersion" -> socketVersion
  )
}
