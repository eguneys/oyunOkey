package oyun.hub
package actorApi

import play.api.libs.json._

case class SendTo(userId: String, message: JsObject)

package map {
  case class Get(id: String)
  case class Tell(id: String, msg: Any)
  case class Ask(id: String, msg: Any)
}

package round {
  case class NbRounds(nb: Int)

  case class FishnetPlay(uci: okey.format.Uci)
}

case class WithUserIds(f: Iterable[String] => Unit)

case object GetUids
case class SocketUids(uids: Set[String])
