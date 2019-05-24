package oyun.base

import play.api.libs.json._

final class PimpedJsObject(js: JsObject) {
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

  def get[A: Reads](key: String): Option[A] =
    (js \ key).asOpt[A]
}
