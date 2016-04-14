package oyun.db

import reactivemongo.api._
import reactivemongo.bson._

trait CollExt { self: dsl with QueryBuilderExt =>
  final implicit class ExtendColl(coll: Coll) {

    def uno[D: BSONDocumentReader](selector: BSONDocument): Fu[Option[D]] =
      coll.find(selector).uno[D]

    def byId[D: BSONDocumentReader](id: String): Fu[Option[D]] = uno[D]($id(id))
    
    def byId[D: BSONDocumentReader](id: Int): Fu[Option[D]] = uno[D]($id(id))

  }
}