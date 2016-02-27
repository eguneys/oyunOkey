package oyun.db
package api

import Implicits._
import play.api.libs.json._
import reactivemongo.bson._

object $find {

  def one[A: TubeInColl](
    q: JsObject,
    modifier: QueryBuilder => QueryBuilder = identity): Fu[Option[A]] =
    one(modifier($query(q)))

  def one[A: TubeInColl](q: QueryBuilder): Fu[Option[A]] =
    q.one[Option[A]] map (_.flatten)

  def byId[ID: Writes, A: TubeInColl](id: ID): Fu[Option[A]] = one($select byId id)
  def byId[A: TubeInColl](id: String): Fu[Option[A]] = byId[String, A](id)
}
