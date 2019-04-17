package oyun.round

import scala.concurrent.duration._

import akka.actor._

import actorApi._, round._
import oyun.game.{ GameRepo, Game, Pov, PlayerRef, Event }
import oyun.game.actorApi.{ WithdrawMasa }

import oyun.hub.actorApi.map._
import oyun.hub.actorApi.round.FishnetPlay
import oyun.hub.Duct

private[round] final class Round(
  gameId: Game.ID,
  dependencies: Round.Dependencies,
  activeTtl: Duration,
  bus: oyun.common.Bus) extends Duct {

  import dependencies._

  private[this] implicit val proxy = new GameProxy(gameId)

  def getGame: Fu[Option[Game]] = proxy.game

  val process: Duct.ReceiveAsync = {
    case p: HumanPlay =>
      handleHumanPlay(p) { pov =>
        player.human(p, this)(pov)
      }

    case FishnetPlay(uci) => handle { game =>
      println("here", game)
      player.fishnet(game, uci)
    }

    // case OutOfTime => proxy withGame { game =>
    //   game.outoftime ?? {
    //     proxy withGame { game =>
    //         self ! PoisonPill
    //         //if (game.abortable) finisher.other(game, _.Aborted)
    //         finisher.other(game, _.Aborted)
    //     }
    //     player.requestFishnet(game)
    //   }
    // }
    case OutOfTime => handle { game =>
      game.outoftime ?? {
        player.requestFishnet(game)
        player.incNbOutOfTime(game)
      }
      if (game.nbOutOfTime > 1) {
        game.masaId ?? { masaId =>
          game.player.playerId ?? { playerId =>
            println("outoftime", masaId, playerId)
            bus.publish(WithdrawMasa(masaId, playerId), 'withdrawMasa)
          }
        }


        finisher.other(game, _.Aborted)
      } else fuccess(Nil)
    }

    // exceptionally we don't block nor publish events
    // if the game is abandoned, then nobody is around to see it
    // we can also terminate this actor
    case Abandon => fuccess {
      proxy withGame { game =>
        game.abandoned ?? {
          //if (game.abortable) finisher.other(game, _.Aborted)
          finisher.other(game, _.Aborted)
        }
      }
    }

    case NoStart => handle { game =>
      game.timeBeforeExpiration.exists(_.centis == 0) ?? {
        finisher.noStart(game)
      }
    }
  }

  protected def handle[A](op: Game => Fu[Events]): Funit =
    handleGame(proxy.game)(op)

  protected def handleHumanPlay(p: HumanPlay)(op: Pov => Fu[Events]): Funit =
    handlePov {
      proxy playerPov p.playerId
    }(op)

  def handle(playerId: String)(op: Pov => Fu[Events]): Funit =
    handlePov(proxy playerPov playerId)(op)

  private def handlePov(pov: Fu[Option[Pov]])(op: Pov => Fu[Events]): Funit = publish {
    pov flatten "pov not found" flatMap { p =>
      op(p)
    }
  }

  private def handleGame(game: Fu[Option[Game]])(op: Game => Fu[Events]): Funit = publish {
    game flatten "game not found" flatMap op
  } recover errorHandler("handleGame")

  private def publish[A](op: Fu[Events]) = op.addEffect { events =>
    if (events.nonEmpty) socketMap.tell(gameId, EventList(events))
  }.void recover errorHandler("publish")

  private def errorHandler(name: String): PartialFunction[Throwable, Unit] = {
    case e: ClientError =>
      println("mon round error", e)
      proxy invalidate
    case e: Exception =>
      logger.warn(s"$name: ${e.getMessage}")
      proxy invalidate
  }
}

object Round {
  private[round] case class Dependencies(
    finisher: Finisher,
    player: Player,
    socketMap: SocketMap
  )
}
