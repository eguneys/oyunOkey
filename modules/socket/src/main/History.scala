package oyun.socket

import play.api.libs.json._

final class History {

  type Message = History.Message

  private var privateVersion = 0

  def version = privateVersion

  def +=(payload: JsObject): Message = {
    privateVersion = privateVersion + 1
    val vmsg = History.Message(payload, privateVersion)
    vmsg
  }
}

object History {
  case class Message(payload: JsObject, version: Int) {
    lazy val fullMsg = payload + ("v" -> JsNumber(version))
  }
}
