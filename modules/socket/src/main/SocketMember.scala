package oyun.socket

import play.api.libs.json.JsValue

trait SocketMember {
  protected val channel: JsChannel
  val userId: Option[String]

  def push(msg: JsValue) {
    channel push msg
  }

  def end {
    channel.end
  }
}
