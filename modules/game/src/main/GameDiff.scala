package oyun.game

import org.joda.time.DateTime
import reactivemongo.bson._

import okey.{ Sides, Side, Clock }
import Game.BSONFields._

import oyun.db.BSON.BSONJodaDateTimeHandler
import oyun.db.ByteArray

private[game] object GameDiff {

  type Set = BSONElement // (String, BSONValue)
  type Unset = BSONElement // (String, BSONBoolean)

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
      d(s"$binaryDiscards.${side.letter}", _.binaryDiscards(side), ByteArray.ByteArrayBSONHandler.write)

      dOpt(s"$binaryOpens.$binaryOpenStates.${side.letter}", _.binaryOpens, (o: Option[BinaryOpens]) => o flatMap { opens =>
        opens.binaryOpenStates(side) map ByteArray.ByteArrayBSONHandler.write
      })

      dOpt(s"$binaryOpensSave.$binaryOpenStates.${side.letter}", _.binaryOpens flatMap { _.save map(_._2) }, (o: Option[BinaryOpens]) => o flatMap { opens =>
        opens.binaryOpenStates(side) map ByteArray.ByteArrayBSONHandler.write
      })

      d(s"$outOfTimes.${side.letter}", _.outOfTimes(side), w.int)
    }

    dOpt(s"$binaryOpens.$binarySeries", _.binaryOpens, (o: Option[BinaryOpens]) => o map { opens =>
      ByteArray.ByteArrayBSONHandler.write(opens.binarySeries)
    })

    dOpt(s"$binaryOpens.$binaryPairs", _.binaryOpens, (o: Option[BinaryOpens]) => o map { opens =>
      ByteArray.ByteArrayBSONHandler.write(opens.binaryPairs)
    })

    dOpt(s"$binaryOpensSave.$binarySeries", _.binaryOpens flatMap { _.save map(_._2) }, (o: Option[BinaryOpens]) => o map { opens =>
      ByteArray.ByteArrayBSONHandler.write(opens.binarySeries)
    })

    dOpt(s"$binaryOpensSave.$binaryPairs", _.binaryOpens flatMap { _.save map(_._2) }, (o: Option[BinaryOpens]) => o map { opens =>
      ByteArray.ByteArrayBSONHandler.write(opens.binaryPairs)
    })

    dOpt(s"$binaryPiecesSave", _.binaryOpens flatMap { _.save map(_._1) }, (o: Option[ByteArray]) => o map { pieces =>
      ByteArray.ByteArrayBSONHandler.write(pieces)
    })

    d(binaryMiddles, _.binaryMiddles, ByteArray.ByteArrayBSONHandler.write)
    d(binaryPlayer, _.binaryPlayer, ByteArray.ByteArrayBSONHandler.write)
    d(turns, _.turns, w.int)
    d(opensLastMove, _.opensLastMove, OpensLastMove.opensLastMoveBSONHandler.write)
    d(status, _.status.id, w.int)

    dOpt(clock, _.clock, (o: Option[Clock]) => o map { c =>
      BSONHandlers.clockBSONWrite(a.createdAt, c)
    })

    (addUa(setBuilder.toList), unsetBuilder.toList)

  }

  private def addUa(sets: List[Set]): List[Set] = sets match {
    case Nil => Nil
    case sets => (Game.BSONFields.movedAt -> BSONJodaDateTimeHandler.write(DateTime.now)) :: sets
  }
}
