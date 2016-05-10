package oyun.socket

import scala.concurrent.duration.Duration

import play.api.libs.json._

final class History(ttl: Duration) {

  type Message = History.Message

  private var privateVersion = 0
  private val messages = oyun.memo.Builder.expiry[Int, Message](ttl)

  def version = privateVersion

  // none if version asked is > to history version
  // none if an event is missing (asked too old version)
  def since(v: Int): Option[List[Message]] =
    if (v > version) None
    else if (v == version) Some(Nil)
    else {
      val msgs = (v + 1 to version).toList flatMap message
      (msgs.size == version - v) option msgs
    }

  private def message(v: Int) = Option(messages getIfPresent v)

  def +=(payload: JsObject): Message = {
    privateVersion = privateVersion + 1
    val vmsg = History.Message(payload, privateVersion)
    messages.put(privateVersion, vmsg)
    vmsg
  }
}

object History {
  case class Message(payload: JsObject, version: Int) {
    lazy val fullMsg = payload + ("v" -> JsNumber(version))
  }
}
