package oyun.db

import reactivemongo.api._
import reactivemongo.api.collections.GenericQueryBuilder
import reactivemongo.bson._

object Types extends Types
object Implicits extends Implicits

trait Types {
  type Coll = reactivemongo.api.collections.bson.BSONCollection

  type QueryBuilder = GenericQueryBuilder[BSONSerializationPack.type]
}

trait Implicits extends Types {

}
