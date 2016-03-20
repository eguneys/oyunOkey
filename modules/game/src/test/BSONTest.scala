package oyun.game

import org.specs2.mutable._

import play.api.libs.json._

import oyun.db.ByteArray

import okey.{ Game => OkeyGame, _ }

import Piece._

import play.modules.reactivemongo.json.BSONFormats._

class BSONTest extends Specification {

  import tube.gameTube

  "game bson handler" should {
    "write" in {
      val game = Game.make(
        game = OkeyGame(okey.variant.Standard),
        players = Player.allSides
      )

      val doc = BSONHandlers.gameBSONHandler write game

      println(Json.toJson(doc))

      val g2 = BSONHandlers.gameBSONHandler read doc

      println(g2.binaryOpens)

      //game must_== g2
      3 must_== 3
    }
  }
}
