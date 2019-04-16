package oyun.game

import oyun.db.{ BSON, ByteArray }
import reactivemongo.bson._
import org.joda.time.DateTime

import okey.{ Sides, Side, Status, EndScoreSheet, Clock }
import okey.variant._

object BSONHandlers {

  implicit val StatusBSONHandler = new BSONHandler[BSONInteger, Status] {
    def read(bsonInt: BSONInteger): Status = Status(bsonInt.value) err s"No such status: ${bsonInt.value}"
    def write(x: Status) = BSONInteger(x.id)
  }

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

  private[game] implicit val sidesPiecesBSONHandler = new BSON[Sides[ByteArray]] {

    import Game.BSONFields._

    def reads(r: BSON.Reader) = {
      val bBytes = Sides("e", "w", "n", "s") map { s =>
        r bytes (s)
      }
      bBytes
    }

    def writes(w: BSON.Writer, o: Sides[ByteArray]) = BSONDocument(
      "e" -> o.eastSide,
      "w" -> o.westSide,
      "n" -> o.northSide,
      "s" -> o.southSide
    )
  }

  private[game] implicit val binaryOpensBSONHandler = new BSON[BinaryOpens] {

    import Game.BSONFields._

    def reads(r: BSON.Reader) = {

      val bOpenStates = r.getO[Sides[Option[ByteArray]]](binaryOpenStates)

      BinaryOpens(
        binarySeries = r bytes binarySeries,
        binaryPairs = r bytes binaryPairs,
        binaryOpenStates = bOpenStates getOrElse Sides[Option[ByteArray]]
      )
    }

    def writes(w: BSON.Writer, o: BinaryOpens) = {
      BSONDocument(
        binarySeries -> o.binarySeries,
        binaryPairs -> o.binaryPairs,
        binaryOpenStates -> o.binaryOpenStates
      )
    }
  }

  private[game] implicit val scoresBSONHandler = new BSON[Variant => EndScoreSheet] {
    // import BSON.MapValue.MapHandler

    def reads(r: BSON.Reader) = variant => {
      val handSum = r int "h"
      val scoreDocs = r get[Map[String, BSONInteger]]("s")

      val scores = scoreDocs map {
        case (k, v) => {
          val flag = okey.Flag(k) err "No such flag: ${k}"
          flag -> okey.FlagScore(v.value)
        }
      }

      EndScoreSheet.byVariant(variant)(handSum, scores)
    }

    def writes(w: BSON.Writer, o: Variant => EndScoreSheet): BSONDocument = {
      o(Standard) |> { sheet =>
        val scores = sheet.scores map {
          case (k, v) => {
            k.id.toString -> BSONInteger(v.map(_.id) | 0)
          }
        }
        BSONDocument(
          "h" -> sheet.handSum,
          "s" -> scores
        )
      }
    }
  }

  implicit val gameBSONHandler = new BSON[Game] {

    import Game.{ BSONFields => F }
    import Player.playerBSONHandler

    def reads(r: BSON.Reader): Game = {
      val realVariant = Variant(r intD F.variant) | okey.variant.Standard
      val nbTurns = r int F.turns
      val winS = r getO[String] F.winnerSide flatMap Side.apply
      val createdAt = r date F.createdAt

      val oEndScores = r getO[Sides[Variant => EndScoreSheet]](F.endScores)
      val oEndStanding = r intO (F.endStanding)

      val List(eastId, westId, northId, southId) = r str F.playerIds grouped 4 toList

      val sidesPid = r.get[Sides[Option[String]]](F.playerPids)

      val sidesUid = r.get[Sides[Option[String]]](F.playerUids)

      val sidesSid = r.get[Sides[Option[String]]](F.playerSids)

      val builder = r.get[Sides[Player.Builder]](F.sidesPlayer)

      val players = Sides(eastId, westId, northId, southId) sideMap {
        case (side, id) =>
          val win = winS map (_ == side)
          builder(side)(side)(id)(sidesPid(side))(sidesUid(side))(sidesSid(side))(oEndScores.map(_(side)(realVariant)))(oEndStanding)(win)
      }

      val bPieces = r.get[Sides[ByteArray]](F.binaryPieces)

      val bDiscards = r.get[Sides[ByteArray]](F.binaryDiscards)

      val bOpens = r.getO[BinaryOpens](F.binaryOpens)


      val saveOpens = r.getO[BinaryOpens](F.binaryOpensSave)
      val saveBoard = r bytesO F.binaryPiecesSave

      val bOpens2 = bOpens map { opens =>

        val saveP = r bytesO F.binaryPiecesSave
        val saveO = r.getO[BinaryOpens](F.binaryOpensSave)

        val save = (saveP, saveO) match {
          case (Some(p), Some(o)) => Some(p, o)
          case _ => None
        }

        opens.copy(save = save)
      }

      val bpp = r bytes F.binaryPlayer

      Game(
        id = r str F.id,
        players = players,
        binaryPieces = bPieces,
        binaryDiscards = bDiscards,
        binaryMiddles = r bytes F.binaryMiddles,
        binarySign = r int F.binarySign toByte,
        binaryOpens = bOpens2,
        binaryPlayer = r bytes F.binaryPlayer,
        clock = r.getO[Side => Clock](F.clock)(clockBSONReader(createdAt)) map (_(Side(nbTurns))),
        opensLastMove = r.get[OpensLastMove](F.opensLastMove)(OpensLastMove.opensLastMoveBSONHandler),
        mode = Mode(r boolD F.rated),
        status = r.get[Status](F.status),
        turns = nbTurns,
        outOfTimes = r.get[Sides[Int]](F.outOfTimes),
        variant = realVariant,
        createdAt = createdAt,
        movedAt = r dateD (F.movedAt, createdAt),
        metadata = Metadata(
          masaId = r strO F.masaId,
          roundAt = r int F.roundAt
        )
      )
    }

    def writes(w: BSON.Writer, o: Game) = BSONDocument(
      F.id -> o.id,
      F.playerIds -> (o.players.map(_.id) mkString),
      F.playerPids -> o.players.mapt(_.playerId),
      F.playerUids -> o.players.mapt(_.userId),
      F.playerSids -> o.players.mapt(_.seatId),
      F.sidesPlayer -> o.players.mapt(p => playerBSONHandler write ((_: Side) => (_: Player.Id) => (_: Player.PlayerId) => (_: Player.UserId) => (_: Player.SeatId) => (_: Player.EndScore) => (_: Player.EndStanding) => (_: Player.Win) => p)),
      F.binaryPieces -> o.binaryPieces,
      F.binaryDiscards -> o.binaryDiscards,
      F.binaryMiddles -> o.binaryMiddles,
      F.binarySign -> o.binarySign,
      F.binaryOpens -> o.binaryOpens,
      F.binaryPiecesSave -> o.binaryOpens.flatMap { _.save map(t => t._1) },
      F.binaryOpensSave -> o.binaryOpens.flatMap { _.save map(t => t._2) },
      F.binaryPlayer -> o.binaryPlayer,
      F.opensLastMove -> OpensLastMove.opensLastMoveBSONHandler.write(o.opensLastMove),
      F.rated -> w.boolO(o.mode.rated),
      F.status -> o.status,
      F.turns -> o.turns,
      F.outOfTimes -> o.outOfTimes,
      F.clock -> (o.clock map { c => clockBSONWrite(o.createdAt, c)}),
      F.variant -> o.variant.exotic.option(o.variant.id).map(w.int),
      F.createdAt -> w.date(o.createdAt),
      F.movedAt -> w.date(o.movedAt),
      F.masaId -> o.metadata.masaId,
      F.roundAt -> o.metadata.roundAt)
  }

  import oyun.db.ByteArray.ByteArrayBSONHandler

  private[game] def clockBSONReader(since: DateTime) = new BSONReader[BSONBinary, Side => Clock] {
    def read(bin: BSONBinary) = BinaryFormat.clock(since).read(
      ByteArrayBSONHandler read bin
    )
  }

  private[game] def clockBSONWrite(since: DateTime, clock: Clock) = ByteArrayBSONHandler write {
    BinaryFormat clock since write clock
  }
}
