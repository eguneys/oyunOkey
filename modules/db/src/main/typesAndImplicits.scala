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
  // hack, this should be in reactivemongo
  implicit final class OyunPimpedQueryBuilder(b: QueryBuilder) {

    def batch(nb: Int): QueryBuilder = b.options(b.options batchSize nb)

    def toList[A: BSONDocumentReader](limit: Option[Int]): Fu[List[A]] =
      limit.fold(b.cursor[A]().collect[List]()) { l =>
        batch(l).cursor[A]().collect[List](l)
      }
  }
}
