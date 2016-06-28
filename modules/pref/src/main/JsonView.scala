package oyun.pref

import play.api.libs.json._

object JsonView {
  implicit val prefJsonWriter = OWrites[Pref] { p =>
    Json.obj(
      "theme" -> p.theme)
  }
}
