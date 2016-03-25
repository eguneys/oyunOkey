package oyun.round

import akka.actor.ActorRef

import okey.{ Status, Side }
import okey.format.{ Uci }
import oyun.game.{ GameRepo, Game, Pov, Progress }

import actorApi.round.{ HumanPlay }


private[round] final class Player(
  finisher: Finisher) {

  def human(play: HumanPlay, round: ActorRef)(pov: Pov): Fu[Events] = play match {
    case HumanPlay(playerId, uci, promiseOption) => pov match {
      case Pov(game, side) if game playableBy side =>
        applyUci(game, side, uci).prefixFailuresWith(s"$pov")
          .fold(errs => fufail(ClientError(errs.shows)), fuccess).flatMap {
          case (progress, move) =>
            (GameRepo save progress) >>
            progress.game.finished.fold(moveFinish(progress.game, side) map { progress.events ::: _ }, {
                funit inject progress.events
              }) >>- promiseOption.foreach(_.success(()))
        } addFailureEffect { e =>
          promiseOption.foreach(_ failure e)
        }
      case Pov(game, side) if game.finished => fufail(ClientError(s"$pov game is finished"))
      case Pov(game, side) if !game.turnOf(side) => fufail(ClientError(s"$pov not your turn"))
      case _ => fufail(ClientError(s"$pov move refused for some reason"))
    }
  }


  private def applyUci(game: Game, side: Side, uci: Uci) = {
    game.toOkey.apply(side, uci.action) map {
      case (ncg, move) => ncg -> move
    }
  }.map {
    case (newChessGame, move) =>
      game.update(newChessGame, move) -> move
  }

  private def moveFinish(game: Game, side: Side): Fu[Events] = {
    game.status match {
      case Status.End => finisher.other(game, _.End)
      case _ => fuccess(Nil)
    }
  }
}
