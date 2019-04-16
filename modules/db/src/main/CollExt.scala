package oyun.db

import scala.collection.breakOut

import reactivemongo.api._
import reactivemongo.bson._

import reactivemongo.api.commands.GetLastError

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


    def byIds[D: BSONDocumentReader, I: BSONValueWriter](ids: Iterable[I], readPreference: ReadPreference): Fu[List[D]] =
      list[D]($inIds(ids))

    def byIds[D: BSONDocumentReader](ids: Iterable[String], readPreference: ReadPreference = ReadPreference.primary): Fu[List[D]] =
      byIds[D, String](ids, readPreference)

    def countSel(selector: BSONDocument): Fu[Int] = coll count selector.some

    def exists(selector: BSONDocument): Fu[Boolean] = countSel(selector).map(0!=)


    def optionsByOrderedIds[D: BSONDocumentReader, I: BSONValueWriter](
      ids: Iterable[I],
      readPreference: ReadPreference = ReadPreference.primary
    )(docId: D => I): Fu[List[Option[D]]] =
      byIds[D, I](ids, readPreference) map { docs =>
        val docsMap: Map[I, D] = docs.map(u => docId(u) -> u)(breakOut)
        ids.map(docsMap.get)(breakOut)
      }

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
      coll.update(selector, $set(field -> value))

    def incFieldUnchecked(selector: Bdoc, field: String, value: Int = 1): Unit =
      coll.update(selector, $inc(field -> value), writeConcern = GetLastError.Unacknowledged)

  }
}
