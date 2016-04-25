package oyun.common

import play.api.libs.json._

object PimpedJson {
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
