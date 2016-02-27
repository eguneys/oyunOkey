package oyun.game

import oyun.db.{ BSON }
import reactivemongo.bson._

import okey.{ Sides }

object BSONHandlers {

  implicit val gameBSONHandler = new BSON[Game] {

    import Game.BSONFields._

    def reads(r: BSON.Reader): Game = {

      val pids = r.get[List[String]](playerIds)
      val (eastId, westId, northId, southId) = (pids.headOption.filter(_.nonEmpty),
        pids.lift(1).filter(_.nonEmpty),
        pids.lift(2).filter(_.nonEmpty),
        pids.lift(3).filter(_.nonEmpty))

      val players = Sides(eastId, westId, northId, southId) sideMap {
        case (_, None) => None
        case (side, Some(id)) => Some(Player(id, side))
      }

      Game(
        id = r str id,
        players = players
      )
    }

    def writes(w: BSON.Writer, o: Game) = BSONDocument(
      id -> o.id,
      playerIds -> o.players.map(op => ~(op map(_.id))).toList
    )
  }
}
