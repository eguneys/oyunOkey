package oyun.game

import oyun.db.{ BSON, ByteArray }
import reactivemongo.bson._

import okey.{ Sides }

object BSONHandlers {

  private[game] implicit val binaryOpensBSONHandler = new BSON[BinaryOpens] {

    import Game.BSONFields._

    def reads(r: BSON.Reader) = {
      val bOpenStates = Sides("e", "w", "n", "s") map { s =>
        r bytesO (binaryOpenStates + s)
      }
      BinaryOpens(
        binarySeries = r bytes binarySeries,
        binaryPairs = r bytes binaryPairs,
        binaryOpenStates = bOpenStates
      )
    }

    def writes(w: BSON.Writer, o: BinaryOpens) = BSONDocument(
    )
  }

  implicit val gameBSONHandler = new BSON[Game] {

    import Game.BSONFields._

    def reads(r: BSON.Reader): Game = {
      val nbTurns = r int turns
      val pids = r.get[List[String]](playerIds)
      val (eastId, westId, northId, southId) = (pids.headOption.filter(_.nonEmpty),
        pids.lift(1).filter(_.nonEmpty),
        pids.lift(2).filter(_.nonEmpty),
        pids.lift(3).filter(_.nonEmpty))

      val players = Sides(eastId, westId, northId, southId) sideMap {
        case (_, None) => None
        case (side, Some(id)) => Some(Player(id, side))
      }

      val bPieces = Sides("e", "w", "n", "s") map { s =>
        r bytes (binaryPieces + s)
      }

      val bDiscards = Sides("e", "w", "n", "s") map { s =>
        r bytes (binaryDiscards + s)
      }

      val bOpens = r.get[BinaryOpens](binaryOpens) some

      Game(
        id = r str id,
        players = players,
        binaryPieces = bPieces,
        binaryDiscards = bDiscards,
        binaryMiddles = r bytes binaryMiddles,
        binarySign = r int binarySign toByte,
        binaryOpens = bOpens,
        binaryPlayer = r bytes binaryPlayer,
        turns = nbTurns
      )
    }

    def writes(w: BSON.Writer, o: Game) = BSONDocument(
      id -> o.id,
      playerIds -> o.players.map(op => ~(op map(_.id))).toList
    )
  }
}
