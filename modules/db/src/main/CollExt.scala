package oyun.db

import reactivemongo.api._
import reactivemongo.bson._

trait CollExt { self: dsl with QueryBuilderExt =>
  final implicit class ExtendColl(coll: Coll) {

    def uno[D: BSONDocumentReader](selector: BSONDocument): Fu[Option[D]] =
      coll.find(selector).uno[D]

    def list[D: BSONDocumentReader](selector: BSONDocument): Fu[List[D]] =
      coll.find(selector).list[D]()

    def list[D: BSONDocumentReader](selector: BSONDocument, max: Int): Fu[List[D]] =
      coll.find(selector).list[D](max)

    def byId[D: BSONDocumentReader](id: String): Fu[Option[D]] = uno[D]($id(id))
    
    def byId[D: BSONDocumentReader](id: Int): Fu[Option[D]] = uno[D]($id(id))

    def countSel(selector: BSONDocument): Fu[Int] = coll count selector.some

    def exists(selector: BSONDocument): Fu[Boolean] = countSel(selector).map(0!=)

    def primitiveOne[V: BSONValueReader](selector: BSONDocument, field: String): Fu[Option[V]] =
      coll.find(selector, $doc(field -> true))
        .uno[BSONDocument]
        .map {
        _ flatMap { _.getAs[V](field) }
      }

    def primitiveOne[V: BSONValueReader](selector: BSONDocument, sort: BSONDocument, field: String): Fu[Option[V]] =
      coll.find(selector, $doc(field -> true))
        .sort(sort)
        .uno[BSONDocument]
        .map {
        _ flatMap { _.getAs[V](field) }
      }

    def updateFieldUnchecked[V: BSONValueWriter](selector: BSONDocument, field: String, value: V) =
      coll.uncheckedUpdate(selector, $set(field -> value))

  }
}
