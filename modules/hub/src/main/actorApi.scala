package oyun.hub
package actorApi

import play.api.libs.json._

case class SendTo(userId: String, message: JsObject)

case class SendTos(userIds: Set[String], message: JsObject)

sealed abstract class Deploy(val key: String)
case object DeployPre extends Deploy("deployPre")
case object DeployPost extends Deploy("deployPost")

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

package captcha {
  case object AnyCaptcha
  case class GetCaptcha(id: String)
  case class ValidCaptcha(id: String, solution: String)
}

package lobby {
  case class ReloadMasas(html: String)
}
