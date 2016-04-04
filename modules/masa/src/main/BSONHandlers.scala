package oyun.masa

import reactivemongo.bson._

import okey.{ Side, Sides }
import oyun.db.BSON

import oyun.game.BSONHandlers._

object BSONHandlers {

  private implicit val statusBSONHandler = new BSONHandler[BSONInteger, Status] {
    def read(bsonInt: BSONInteger): Status = Status(bsonInt.value) err s"No such status: ${bsonInt.value}"
    def write(x: Status) = BSONInteger(x.id)
  }

  implicit val masaHandler = new BSON[Masa] {
    def reads(r: BSON.Reader) = {
      Masa(
        id = r str "_id",
        status = r.get[Status]("status"),
        system = System.default
      )
    }

    def writes(w: BSON.Writer, o: Masa) = BSONDocument(
      "_id" -> o.id,
      "status" -> o.status
    )
  }

  implicit val playerHandler = new BSON[Player] {
    def reads(r: BSON.Reader) = {
      Player(
        _id = r str "_id",
        masaId = r str "mid",
        active = r boolD "a",
        side = Side(r str "d") err s"No such side:",
        score = r intD "s",
        magicScore = r int "m"
      )
    }

    def writes(w: BSON.Writer, o: Player) = BSONDocument(
      "_id" -> o.id,
      "mid" -> o.masaId,
      "a" -> w.boolO(o.active),
      "d" -> o.side.letter.toString,
      "s" -> w.intO(o.score),
      "m" -> o.magicScore

    )
  }

  implicit val pairingHandler = new BSON[Pairing] {
    def reads(r: BSON.Reader) = {
      Pairing(
        id = r str "_id",
        masaId = r str "mid",
        status = okey.Status(r int "s") err "masa pairing status",
        playerIds = r.get[Sides[String]]("pids"),
        scores = r.get[List[Int]]("ss")
      )
    }

    def writes(w: BSON.Writer, o: Pairing) = BSONDocument(
      "_id" -> o.id,
      "mid" -> o.masaId,
      "s" -> o.status.id,
      "pids" -> o.playerIds,
      "ss" -> o.scores.toList
    )
  }
}
