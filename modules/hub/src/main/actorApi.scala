package oyun.hub
package actorApi

package map {
  case class Get(id: String)
  case class Tell(id: String, msg: Any)
  case class Ask(id: String, msg: Any)
}

case object GetUids
case class SocketUids(uids: Set[String])
