package oyun.db

import scala.util.{ Try, Success, Failure }

import reactivemongo.bson._
import Types.Coll

trait InColl[A] { implicit def coll: Types.Coll }

trait Tube[Doc] extends BSONDocumentReader[Option[Doc]]

case class BsTube[Doc](handler: BSONHandler[BSONDocument, Doc]) extends Tube[Doc] {

  def read(bson: BSONDocument): Option[Doc] = handler readTry bson match {
    case Success(doc) => Some(doc)
    case Failure(err) =>
      logger.error(s"[tube] Cannot read ${oyun.db.BSON.debug(bson)}\n$err\n${err.printStackTrace}")
      None
  }

  def write(doc: Doc): BSONDocument = handler write doc

  def inColl(c: Coll): BsTubeInColl[Doc] =
    new BsTube[Doc](handler) with InColl[Doc] { def coll = c }

}
