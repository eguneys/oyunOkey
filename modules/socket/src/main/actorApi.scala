package oyun.socket
package actorApi

import play.api.libs.json.JsObject

case class Connected[M <: SocketMember](
  enumerator: JsEnumerator,
  member: M)

case class Ping(uid: String)
case class PingVersion(uid: String, version: Int)
case object Broom

case class Quit(uid: String)

case class SocketEnter[M <: SocketMember](uid: String, member: M)
case class SocketLeave[M <: SocketMember](uid: String, member: M)

case class Resync(uid: String)

case object GetVersion

case class SendToFlag(flag: String, message: JsObject)

case object PopulationTell
case class NbMembers(nb: Int)
