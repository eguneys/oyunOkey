package oyun.db

import play.api.libs.json._
import Types._

object $query {
  import play.modules.reactivemongo.json._

  def apply[A: InColl](q: JsObject) = builder query q

  def builder[A: InColl] = implicitly[InColl[A]].coll.genericQueryBuilder
}
