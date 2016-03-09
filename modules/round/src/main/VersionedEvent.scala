package oyun.round

import play.api.libs.json._

case class VersionedEvent(
  version: Int,
  typ: String,
  encoded: Either[String, JsValue]) {

  lazy val decoded: JsValue = encoded match {
    case Left(s) => Json parse s
    case Right(js) => js
  }
}
