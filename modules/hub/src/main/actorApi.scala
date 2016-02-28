package oyun.hub
package actorApi

package map {
  case class Get(id: String)
}

case object GetUids
case class SocketUids(uids: Set[String])
