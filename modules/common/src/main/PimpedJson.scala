package oyun.common

import play.api.libs.json._

object PimpedJson {

  def anyValWriter[O, A: Writes](f: O => A) = Writes[O] { o =>
    Json toJson f(o)
  }

  def stringIsoWriter[O](iso: Iso[String, O]): Writes[O] = anyValWriter[O, String](iso.to)

  def stringIsoFormat[O](iso: Iso[String, O]): Format[O] = Format[O](
    Reads.of[String] map iso.from,
    Writes { o => JsString(iso to o) }
  )

  implicit final class LilaPimpedJsonObject(js: JsObject) {
    def str(key: String): Option[String] =
      (js \ key).asOpt[String]

    def int(key: String): Option[Int] =
      (js \ key).asOpt[Int]

    def boolean(key: String): Option[Boolean] =
      (js \ key).asOpt[Boolean]

    def obj(key: String): Option[JsObject] =
      (js \ key).asOpt[JsObject]

    def noNull = JsObject {
      js.fields collect {
        case (key, value) if value != JsNull => key -> value
      }
    }
  }

  implicit final class LilaPimpedJsValue(val js: JsValue) extends AnyVal {
    def boolean(key: String): Option[Boolean] =
      js.asOpt[JsObject] flatMap { obj =>
        (obj \ key).asOpt[Boolean]
      }
  }
}
