package oyun.fishnet

import org.joda.time.DateTime

import oyun.game.{ Game }

final class Player(moveDb: MoveDB) {

  def apply(game: Game): Funit = game.aiLevel ?? { level =>
    makeWork(game, level) addEffect moveDb.add void
  } recover {
    case e: Exception => logger.info(e.getMessage)
  }

  private def makeWork(game: Game, level: Int): Fu[Work.Move] =
    //if (game.toOkey.situation playable true)
    if (true)
      funit inject Work.Move(
        _id = Work.makeId,
        game = Work.Game(
          game = game
        ),
        level = level,
        acquired = none,
        createdAt = DateTime.now)
    else fufail(s"[fishnet] invalid position on ${game.id}")
}
