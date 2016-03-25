package oyun.db
package api

import play.api.libs.json._
import reactivemongo.bson._

object $update {

  import play.modules.reactivemongo.json._

  def apply[A: InColl, B: BSONDocumentWriter](selector: JsObject, update: B, upsert: Boolean = false, multi: Boolean = false): Funit =
    implicitly[InColl[A]].coll.update(selector, update, upsert = upsert, multi = multi).void
}
