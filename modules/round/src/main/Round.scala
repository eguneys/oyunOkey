package oyun.round

import scala.concurrent.duration._

import akka.actor._

import actorApi._, round._
import oyun.game.{ GameRepo, Game, Pov, PlayerRef, Event }
import oyun.hub.actorApi.map._
import oyun.hub.SequentialActor

private[round] final class Round(
  gameId: String,
  player: Player,
  socketHub: ActorRef,
  activeTtl: Duration) extends SequentialActor {

  context setReceiveTimeout activeTtl

  def process = {
    case ReceiveTimeout => fuccess {
      self ! SequentialActor.Terminate
    }

    case p: HumanPlay =>
      handle(p.playerId) { pov =>
        player.human(p, self)(pov)
      }
  }

  def handle(playerId: String)(op: Pov => Fu[Events]): Funit =
    handlePov((GameRepo pov PlayerRef(gameId, playerId)))(op)

  private def handlePov(pov: Fu[Option[Pov]])(op: Pov => Fu[Events]): Funit = publish {
    pov flatten "pov not found" flatMap { p =>
      op(p)
    }
  }

  private def publish[A](op: Fu[Events]) = op.addEffect { events =>
    if (events.nonEmpty) socketHub ! Tell(gameId, EventList(events))
  }.void recover errorHandler("publish")

  private def errorHandler(name: String): PartialFunction[Throwable, Unit] = {
    case e: ClientError => println("mon round error", e)
    case e: Exception => logger.warn(s"$name: ${e.getMessage}")
  }
}
