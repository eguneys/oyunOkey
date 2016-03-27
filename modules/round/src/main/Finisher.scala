package oyun.round

import okey.{ Status, Side, Sides, EndScoreSheet }

import oyun.game.actorApi.{ FinishGame }
import oyun.game.{ GameRepo, Game, Pov, Progress }

private[round] final class Finisher(
  bus: oyun.common.Bus) {

  def other(
    game: Game,
    status: Status.type => Status, // why?
    result: Option[Sides[EndScoreSheet]]
  ): Fu[Events] =
    apply(game, status, result)

  private def apply(
    game: Game,
    makeStatus: Status.type => Status,
    result: Option[Sides[EndScoreSheet]]): Fu[Events] = {
    val status = makeStatus(Status)
    val prog = game.finish(status, result)
    fuccess(prog.game) flatMap { g =>
      GameRepo.finish(
        id = g.id,
        status = prog.game.status,
        result = result) >> {
        val finish = FinishGame(g)
        GameRepo game g.id foreach { newGame =>
          bus.publish(finish.copy(game = newGame | g), 'finishGame)
        }
        funit inject prog.events
      }
    }
  }

}
