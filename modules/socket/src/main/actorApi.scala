package oyun.socket
package actorApi

case class Connected[M <: SocketMember](
  enumerator: JsEnumerator,
  member: M)

case class Ping(uid: String)
case class PingVersion(uid: String, version: Int)
case object Broom

case class Quit(uid: String)

case class Resync(uid: String)

case object GetVersion
