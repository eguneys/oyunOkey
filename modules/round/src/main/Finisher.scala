package oyun.round

import okey.{ Status, Side }

import oyun.game.actorApi.{ FinishGame }
import oyun.game.{ GameRepo, Game, Pov, Progress }

private[round] final class Finisher(
  bus: oyun.common.Bus) {

  def other(
    game: Game,
    status: Status.type => Status // why?
  ): Fu[Events] =
    apply(game, status)

  private def apply(
    game: Game,
    makeStatus: Status.type => Status): Fu[Events] = {
    val status = makeStatus(Status)
    val prog = game.finish(status)
    fuccess(prog.game) flatMap { g =>
      GameRepo.finish(
        id = g.id,
        status = prog.game.status) >> {
        val finish = FinishGame(g)
        GameRepo game g.id foreach { newGame =>
          bus.publish(finish.copy(game = newGame | g), 'finishGame)
        }
        funit inject prog.events
      }
    }
  }

}
