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

  implicit final class LilaPimpedJsValue(val js: JsValue) extends AnyVal {
    def boolean(key: String): Option[Boolean] =
      js.asOpt[JsObject] flatMap { obj =>
        (obj \ key).asOpt[Boolean]
      }
  }
}
