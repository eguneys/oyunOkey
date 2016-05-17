package oyun.masa

import reactivemongo.bson._

import okey.variant.Variant
import okey.{ Side, Sides }
import oyun.db.BSON

import oyun.game.Mode
import oyun.game.BSONHandlers._

object BSONHandlers {

  private implicit val statusBSONHandler = new BSONHandler[BSONInteger, Status] {
    def read(bsonInt: BSONInteger): Status = Status(bsonInt.value) err s"No such status: ${bsonInt.value}"
    def write(x: Status) = BSONInteger(x.id)
  }

  private implicit val masaClockBSONHandler = Macros.handler[MasaClock]

  implicit val masaHandler = new BSON[Masa] {
    def reads(r: BSON.Reader) = {
      val variant = r.intO("variant").fold[Variant](Variant.default)(Variant.orDefault)
      Masa(
        id = r str "_id",
        name = r str "name",
        status = r.get[Status]("status"),
        system = System.default,
        clock = r.get[MasaClock]("clock"),
        rounds = r int "rounds",
        variant = variant,
        mode = r.intO("mode") flatMap Mode.apply getOrElse Mode.Rated,
        allowAnon = r boolD "allowAnon",
        nbPlayers = r int "nbPlayers",
        nbRounds = r int "nbRounds",
        createdAt = r date "createdAt",
        createdBy = r str "createdBy",
        winnerId = r strO "winner"
      )
    }

    def writes(w: BSON.Writer, o: Masa) = BSONDocument(
      "_id" -> o.id,
      "name" -> o.name,
      "status" -> o.status,
      "clock" -> o.clock,
      "rounds" -> o.rounds,
      "variant" -> o.variant.some.filterNot(_.standard).map(_.id),
      "mode" -> o.mode.some.filterNot(_.rated).map(_.id),
      "allowAnon" -> w.boolO(o.allowAnon),
      "nbPlayers" -> o.nbPlayers,
      "nbRounds" -> o.nbRounds,
      "createdAt" -> w.date(o.createdAt),
      "createdBy" -> w.str(o.createdBy),
      "winner" -> o.winnerId
    )
  }

  implicit val playerBSONHandler = new BSON[Player] {
    def reads(r: BSON.Reader) = {
      Player(
        _id = r str "_id",
        masaId = r str "mid",
        userId = r strO "uid",
        active = r boolD "a",
        side = Side(r str "d") err s"No such side:",
        score = r intD "s",
        rating = r intO "r",
        ratingDiff = r intD "p",
        aiLevel = r intO "al",
        magicScore = r int "m",
        createdAt = r date "createdAt"
      )
    }

    def writes(w: BSON.Writer, o: Player) = BSONDocument(
      "_id" -> o.id,
      "mid" -> o.masaId,
      "uid" -> o.userId,
      "a" -> w.boolO(o.active),
      "d" -> o.side.letter.toString,
      "s" -> w.intO(o.score),
      "r" -> w.intO(o.rating | 0),
      "p" -> w.intO(o.ratingDiff),
      "al" ->  w.intO(o.aiLevel.getOrElse(0)),
      "m" -> o.magicScore,
      "createdAt" -> w.date(o.createdAt)
    )
  }

  implicit val pairingHandler = new BSON[Pairing] {
    def reads(r: BSON.Reader) = {
      val pids = r.get[Sides[String]]("pids")
      Pairing(
        id = r str "_id",
        masaId = r str "mid",
        status = okey.Status(r int "s") err "masa pairing status",
        playerIds = pids,
        round = r.get[Int]("mr"),
        scores = r.get[List[Int]]("ss"),
        winner = r getO[String] "w"
      )
    }

    def writes(w: BSON.Writer, o: Pairing) = BSONDocument(
      "_id" -> o.id,
      "mid" -> o.masaId,
      "s" -> o.status.id,
      "pids" -> o.playerIds,
      "mr" -> o.round,
      "ss" -> o.scores.toList,
      "w" -> o.winner
    )
  }
}
