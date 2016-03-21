package oyun.game

import org.specs2.mutable._

import play.api.libs.json._

import oyun.db.ByteArray

import okey.{ Player => OkeyPlayer, Game => OkeyGame, _ }

import Piece._

import play.modules.reactivemongo.json.BSONFormats._

class DiffTest extends Specification {

  import tube.gameTube

  "game diff" should {
    "binary opens" in {
      val table = okey.format.Visual.<<("""
r12
r1r1
r2r2
""")

      val situation = Situation(table, OkeyPlayer(EastSide, drawMiddle = true))
      val ogame = OkeyGame(situation.table, situation.player)

      val game = Game.make(ogame, Player.allSides)

      ogame(EastSide, OpenPairs(R2.w)).toOption map {
        case (ng, move) =>

          val progress = game.update(
            game = ng,
            move = move
          )

          val diff = GameDiff(progress.origin, progress.game)
          println(diff._1 mkString "\n")
          println("xx")
          println(diff._2 mkString "\n")
          println("binary opens")
          println(progress.game.binaryOpens)

          println(progress.game.toOkey)

          val doc = BSONHandlers.gameBSONHandler.write(progress.game)
          val doc2 = BSONHandlers.gameBSONHandler.write(progress.origin)
          println(Json.toJson(doc))

      }

      3 must_== 3
    }
  }
}
