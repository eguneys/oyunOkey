package oyun.round

import akka.actor.ActorRef

import okey.{ Status, Side }
import okey.format.{ Uci }
import oyun.game.{ GameRepo, Game, Pov, Progress }

import actorApi.round.{ HumanPlay }


private[round] final class Player(
  fishnetPlayer: oyun.fishnet.Player,
  finisher: Finisher) {

  def human(play: HumanPlay, round: ActorRef)(pov: Pov)(implicit proxy: GameProxy): Fu[Events] = play match {
    case HumanPlay(playerId, uci, promiseOption) => pov match {
      case Pov(game, side) if game playableBy side =>
        applyUci(game, side, uci).prefixFailuresWith(s"$pov")
          .fold(errs => fufail(ClientError(errs.shows)), fuccess).flatMap {
          case (progress, move) =>
            (proxy save progress) >>
            progress.game.finished.fold(
              moveFinish(progress.game, side) map { progress.events ::: _ }, {
                funit addEffect {
                  case _ =>
                    if (progress.game.playableByAi) requestFishnet(progress.game)
                } inject progress.events
              }) >>- promiseOption.foreach(_.success(()))
        } addFailureEffect { e =>
          promiseOption.foreach(_ failure e)
        }
      case Pov(game, side) if game.finished => fufail(ClientError(s"$pov game is finished"))
      case Pov(game, side) if !game.turnOf(side) => fufail(ClientError(s"$pov not your turn"))
      case _ => fufail(ClientError(s"$pov move refused for some reason"))
    }
  }

  def requestFishnet(game: Game) = game.playableByAi ?? fishnetPlayer(game)

  private def applyUci(game: Game, side: Side, uci: Uci) = {
    game.toOkey.apply(side, uci.action) map {
      case (ncg, move) => ncg -> move
    }
  }.map {
    case (newChessGame, move) =>
      game.update(newChessGame, move) -> move
  }

  private def moveFinish(game: Game, side: Side)(implicit proxy: GameProxy): Fu[Events] = {
    lazy val situation = game.toOkey.situation
    lazy val result = situation.endScores
    lazy val winner = situation.winner
    game.status match {
      case Status.NormalEnd => finisher.other(game, _.NormalEnd, result, winner)
      case Status.MiddleEnd => finisher.other(game, _.MiddleEnd, result, none)
      case _ => fuccess(Nil)
    }
  }
}
