package oyun.socket
package actorApi

import play.api.libs.json.JsObject

case class Connected[M <: SocketMember](
  enumerator: JsEnumerator,
  member: M)

case class Ping(uid: String)
case class PingVersion(uid: String, version: Int)
case object Broom

case class Quit(uid: Socket.Uid)

case class SocketEnter[M <: SocketMember](uid: Socket.Uid, member: M)
case class SocketLeave[M <: SocketMember](uid: Socket.Uid, member: M)

case class Resync(uid: Socket.Uid)

case object GetVersion

case class SendToFlag(flag: String, message: JsObject)

case object PopulationTell
case class NbMembers(nb: Int)
