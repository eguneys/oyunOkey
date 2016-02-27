package oyun.db
package api

import reactivemongo.bson._
import Types.Coll

object $insert {
  import play.modules.reactivemongo.json._

  def bson[A: BsTubeInColl](doc: A): Funit = bson {
    implicitly[BsTube[A]].handler write doc
  }

  def bson[A: InColl](bs: BSONDocument): Funit =
    implicitly[InColl[A]].coll insert bs void
}
