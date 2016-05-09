package oyun.round

import scala.concurrent.duration._

import akka.actor._

import actorApi._, round._
import oyun.game.{ GameRepo, Game, Pov, PlayerRef, Event }
import oyun.hub.actorApi.map._
import oyun.hub.actorApi.round.FishnetPlay
import oyun.hub.SequentialActor

private[round] final class Round(
  gameId: String,
  finisher: Finisher,
  player: Player,
  socketHub: ActorRef,
  activeTtl: Duration) extends SequentialActor {

  context setReceiveTimeout activeTtl

  implicit val proxy = new GameProxy(gameId)

  def process = {
    case ReceiveTimeout => fuccess {
      self ! SequentialActor.Terminate
    }

    case p: HumanPlay =>
      handleHumanPlay(p) { pov =>
        player.human(p, self)(pov)
      }

    case FishnetPlay(uci) => handle { game =>
      player.fishnet(game, uci)
    }

    case OutOfTime => proxy withGame { game =>
      println("game outoftime", game.outoftime)
      game.outoftime ?? player.requestFishnet(game)
    }

    // exceptionally we don't block nor publish events
    // if the game is abandoned, then nobody is around to see it
    // we can also terminate this actor
    case Abandon => fuccess {
      proxy withGame { game =>
        game.abandoned ?? {
          self ! PoisonPill
          //if (game.abortable) finisher.other(game, _.Aborted)
          finisher.other(game, _.Aborted)
        }
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
    if (events.nonEmpty) socketHub ! Tell(gameId, EventList(events))
  }.void recover errorHandler("publish")

  private def errorHandler(name: String): PartialFunction[Throwable, Unit] = {
    case e: ClientError => println("mon round error", e)
    case e: Exception => logger.warn(s"$name: ${e.getMessage}")
  }
}
