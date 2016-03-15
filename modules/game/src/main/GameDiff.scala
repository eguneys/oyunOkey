package oyun.game

import org.joda.time.DateTime
import reactivemongo.bson._

import okey.{ Sides, Side }
import Game.BSONFields._

import oyun.db.BSON.BSONJodaDateTimeHandler
import oyun.db.ByteArray

private[game] object GameDiff {

  type Set = (String, BSONValue)
  type Unset = (String, BSONBoolean)

  def apply(a: Game, b: Game): (List[Set], List[Unset]) = {
    val setBuilder = scala.collection.mutable.ListBuffer[Set]()
    val unsetBuilder = scala.collection.mutable.ListBuffer[Unset]()

    def d[A, B <: BSONValue](name: String, getter: Game => A, toBson: A => B) {
      val (va, vb) = (getter(a), getter(b))
      if (va != vb) {
        if (vb == None || vb == null || vb == "") unsetBuilder += (name -> BSONBoolean(true))
        else setBuilder += name -> toBson(vb)
      }
    }

    def dOpt[A, B <: BSONValue](name: String, getter: Game => A, toBson: A => Option[B]) {
      val (va, vb) = (getter(a), getter(b))
      if (va != vb) {
        if (vb == None || vb == null || vb == "") unsetBuilder += (name -> BSONBoolean(true))
        else toBson(vb) match {
          case None => unsetBuilder += (name -> BSONBoolean(true))
          case Some(x) => setBuilder += name -> x
        }
      }
    }

    val w = oyun.db.BSON.writer

    Side.all map { side =>
      d(s"$binaryPieces.${side.letter}", _.binaryPieces(side), ByteArray.ByteArrayBSONHandler.write)
    }
    d(binaryPlayer, _.binaryPlayer, ByteArray.ByteArrayBSONHandler.write)
    d(turns, _.turns, w.int)

    (addUa(setBuilder.toList), unsetBuilder.toList)

  }

  private def addUa(sets: List[Set]): List[Set] = sets match {
    case Nil => Nil
    case sets => (Game.BSONFields.updatedAt -> BSONJodaDateTimeHandler.write(DateTime.now)) :: sets
  }
}
