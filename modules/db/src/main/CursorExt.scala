package oyun.db

import scala.collection.generic.CanBuildFrom

import reactivemongo.api._
import reactivemongo.bson._

trait CursorExt { self: dsl =>
  final implicit class ExtendCursor[A: BSONDocumentReader](val c: Cursor[A]) {
    // like collect, but with stopOnError defaulting to false
    def gather[M[_]](upTo: Int = Int.MaxValue)(implicit cbf: CanBuildFrom[M[_], A, M[A]]): Fu[M[A]] =
      c.collect[M](upTo, Cursor.ContOnError[M[A]]())
  }
}
