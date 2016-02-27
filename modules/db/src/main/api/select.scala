package oyun.db
package api

import play.api.libs.json._

object $select {
  def byId[A: Writes](id: A) = Json.obj("_id" -> id)
}
