package oyun.game

import oyun.db.{ BSON, ByteArray }
import reactivemongo.bson._

import okey.{ Sides, Side }

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

      val bOpenStates = r.get[Sides[ByteArray]](binaryOpenStates) map {
        case s if s.isEmpty => None
        case s => Some(s)
      }

      BinaryOpens(
        binarySeries = r bytes binarySeries,
        binaryPairs = r bytes binaryPairs,
        binaryOpenStates = bOpenStates
      )
    }

    def writes(w: BSON.Writer, o: BinaryOpens) = {
      val bos = o.binaryOpenStates map (s => ~s)
      BSONDocument(
        binarySeries -> o.binarySeries,
        binaryPairs -> o.binaryPairs,
        binaryOpenStates -> bos
      )
    }
  }

  implicit val gameBSONHandler = new BSON[Game] {

    import Game.BSONFields._
    import Player.playerBSONHandler

    def reads(r: BSON.Reader): Game = {
      val nbTurns = r int turns

      val List(eastId, westId, northId, southId) = r str playerIds grouped 4 toList

      val sidesPid = r.get[Sides[Option[String]]](playerPids)

      val builder = r.get[Sides[Player.Builder]](sidesPlayer)

      val players = Sides(eastId, westId, northId, southId) sideMap {
        case (side, id) => builder(side)(side)(id)(sidesPid(side))
      }

      val bPieces = r.get[Sides[ByteArray]](binaryPieces)

      val bDiscards = r.get[Sides[ByteArray]](binaryDiscards)

      val bOpens = r.getO[BinaryOpens](binaryOpens)

      Game(
        id = r str id,
        players = players,
        binaryPieces = bPieces,
        binaryDiscards = bDiscards,
        binaryMiddles = r bytes binaryMiddles,
        binarySign = r int binarySign toByte,
        binaryOpens = bOpens,
        binaryPlayer = r bytes binaryPlayer,
        turns = nbTurns,
        metadata = Metadata(
          masaId = r strO masaId
        )
      )
    }

    def writes(w: BSON.Writer, o: Game) = BSONDocument(
      id -> o.id,
      playerIds -> (o.players.map(_.id) mkString),
      playerPids -> o.players.mapt(_.playerId),
      sidesPlayer -> o.players.mapt(p => playerBSONHandler write ((_: Side) => (_: Player.Id) => (_: Player.PlayerId) => p)),
      binaryPieces -> o.binaryPieces,
      binaryDiscards -> o.binaryDiscards,
      binaryMiddles -> o.binaryMiddles,
      binarySign -> o.binarySign,
      binaryOpens -> o.binaryOpens,
      binaryPlayer -> o.binaryPlayer,
      turns -> o.turns,
      masaId -> o.metadata.masaId
    )
  }
}
