package oyun.round

import akka.actor.ActorRef

import okey.format.{ Uci }
import oyun.game.{ GameRepo, Game, Pov, Progress }

import actorApi.round.{ HumanPlay }


private[round] final class Player {

  def human(play: HumanPlay, round: ActorRef)(pov: Pov): Fu[Events] = play match {
    case HumanPlay(playerId, uci, promiseOption) => pov match {
      case Pov(game, side) if game playableBy side => {
        {
          game.toOkey.apply(side, uci.action) map {
            case (ncg, move) => ncg -> move
          }
        }.map {
          case (newChessGame, move) =>
            game.update(newChessGame, move) -> move
        }
      }.prefixFailuresWith(s"$pov")
        .fold(errs => ClientErrorException.future(errs.shows), fuccess).flatMap {
          case (progress, move) =>
            (GameRepo save progress) inject progress.events
        }.addFailureEffect { e =>
          promiseOption.foreach(_ failure e)
        }
      case Pov(game, side) if !game.turnOf(side) => ClientErrorException.future(s"$pov not your turn")
      case _ => ClientErrorException.future(s"$pov move refused for some reason")

    }
  }

}
