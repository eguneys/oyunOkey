package oyun.masa

import reactivemongo.bson._

import okey.{ Side, Sides }
import oyun.db.BSON

object BSONHandlers {

  implicit def sidesOptionBSONHandler[T](implicit reader: BSONReader[_ <: BSONValue, T], writer: BSONWriter[T, _ <: BSONValue])  = new BSON[Sides[Option[T]]] {
    def reads(r: BSON.Reader) = {
      val bSides = Sides("e", "w", "n", "s") map { s =>
        r getO[T](s)
      }
      bSides
    }

    def writes(w: BSON.Writer, o: Sides[Option[T]]) = (o sideMap { (side, a) =>
      a map (k => BSONDocument(side.letter.toString -> k))
    }).foldLeft(BSONDocument()) { (acc, d) => d.fold(acc) (acc ++) }
  }

  implicit def sidesBSONHandler[T](implicit reader: BSONReader[_ <: BSONValue, T], writer: BSONWriter[T, _ <: BSONValue])  = new BSON[Sides[T]] {
    def reads(r: BSON.Reader) = {
      val bSides = Sides("e", "w", "n", "s") map { s =>
        r get[T](s)
      }
      bSides
    }

    def writes(w: BSON.Writer, o: Sides[T]) = BSONDocument(
      "e" -> o.eastSide,
      "w" -> o.westSide,
      "n" -> o.northSide,
      "s" -> o.southSide
    )
  }

  implicit val masaHandler = new BSON[Masa] {
    def reads(r: BSON.Reader) = {
      Masa(
        id = r str "_id"
      )
    }

    def writes(w: BSON.Writer, o: Masa) = BSONDocument(
      "_id" -> o.id
    )
  }

  implicit val playerHandler = new BSON[Player] {
    def reads(r: BSON.Reader) = {
      Player(
        _id = r str "_id",
        masaId = r str "mid",
        active = r bool "a",
        side = Side(r str "s")
      )
    }

    def writes(w: BSON.Writer, o: Player) = BSONDocument(
      "_id" -> o.id,
      "mid" -> o.masaId,
      "a" -> o.active,
      "s" -> o.side.letter.toString
    )
  }

  implicit val pairingHandler = new BSON[Pairing] {
    def reads(r: BSON.Reader) = {
      Pairing(
        id = r str "_id",
        masaId = r str "mid",
        playerIds = r.get[Sides[String]]("pids")
      )
    }

    def writes(w: BSON.Writer, o: Pairing) = BSONDocument(
      "_id" -> o.id,
      "mid" -> o.masaId,
      "pids" -> o.playerIds
    )
  }
}
