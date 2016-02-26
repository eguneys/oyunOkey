package oyun.socket

import play.api.libs.json._
import play.api.libs.iteratee.{ Iteratee, Enumerator }

trait WithSocket {
  type JsChannel = play.api.libs.iteratee.Concurrent.Channel[JsValue]
  type JsEnumerator = Enumerator[JsValue]
  type JsIteratee = Iteratee[JsValue, _]
  type JsSocketHandler = (JsIteratee, JsEnumerator)
}
