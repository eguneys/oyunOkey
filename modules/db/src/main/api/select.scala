package oyun.db
package api

import play.api.libs.json._

object $select {

  def apply[A: Writes](id: A): JsObject = byId(id)

  def byId[A: Writes](id: A) = Json.obj("_id" -> id)
}
