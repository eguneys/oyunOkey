package oyun.socket

import play.api.libs.json._

trait Historical[M <: SocketMember] { self: SocketTrouper[M] =>
  protected val history: History

  protected type Message = History.Message

  def notifyVersion[A: Writes](t: String, data: A) {
    val vmsg = history.+=(makeMessage(t, data))
    val send = sendMessage(vmsg) _
    members.values foreach send
  }

  def sendMessage(message: Message)(member: M) {
    member push {
      message.fullMsg
    }
  }

  def sendMessage(member: M)(message: Message) {
    sendMessage(message)(member)
  }
}
