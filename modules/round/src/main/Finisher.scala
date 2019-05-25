package oyun.round

import okey.{ Status, Side, Sides, EndScoreSheet }

import oyun.game.actorApi.{ FinishGame }
import oyun.game.{ GameRepo, Game, Pov, Progress }
import oyun.user.{ User, UserRepo }

private[round] final class Finisher(
  perfsUpdater: PerfsUpdater,
  bus: oyun.common.Bus) {

  def noStart(game: Game)(implicit proxy: GameProxy): Fu[Events] =
    game.playerWhoDidNotMove ?? { culprit =>
      apply(game, _.Aborted, None, None)
    }

  def other(
    game: Game,
    status: Status.type => Status, // why?
    result: Option[Sides[EndScoreSheet]] = None,
    winner: Option[Side] = None
  )(implicit proxy: GameProxy): Fu[Events] =
    apply(game, status, result, winner)

  private def apply(
    game: Game,
    makeStatus: Status.type => Status,
    result: Option[Sides[EndScoreSheet]],
    winner: Option[Side] = None
  )(implicit proxy: GameProxy): Fu[Events] = {
    val status = makeStatus(Status)
    val endStanding = result map okey.variant.Variant.endStanding
    val prog = game.finish(status, result, endStanding, winner)
    fuccess(prog.game) flatMap { g =>
      proxy.save(prog) >>
        GameRepo.finish(
          id = g.id,
          status = prog.game.status,
          result = result,
          standing = endStanding,
          winnerSide = winner,
          winnerId = winner flatMap { g.player(_).userId }) >> {
          UserRepo.pair(g.players map (_.userId)).flatMap {
            case (users) => {
              val finish = FinishGame(g, users)
              updateCountAndPerfs(finish) inject {
                GameRepo game g.id foreach { newGame =>
                  bus.publish(finish.copy(game = newGame | g), 'finishGame)
                }
                prog.events :+ oyun.game.Event.EndData(g)
              }
            }
          }
        }
    }
  } >>- proxy.invalidate


  private def updateCountAndPerfs(finish: FinishGame): Funit =
    (!finish.game.aborted) ?? {
      val users = finish.users.sequenceSides
      users ?? {
        case (users) =>
          // perfsUpdater.save(finish.game, users)
          funit
      } zip (users ?? { _.map(incNbGames(finish.game)).sequenceFu void }) void
    }

  private def incNbGames(game: Game)(user: User): Funit = game.finished ?? {
    UserRepo.incNbGames(user.id, game.rated, game.hasAi,
      result = game.endStandingByUser(user.id) | 0)
  }
}
