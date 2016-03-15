package oyun.round

import akka.actor._

import actorApi._, round._
import oyun.game.{ GameRepo, Game, Pov, PlayerRef, Event }
import oyun.hub.actorApi.map._

private[round] final class Round(
  gameId: String,
  player: Player,
  socketHub: ActorRef
) extends Actor {

  def receive = {
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

  private def publish[A](op: Fu[Events]) = op addEffect { events =>
    if (events.nonEmpty) socketHub ! Tell(gameId, EventList(events))
  } addFailureEffect {
    case e: ClientErrorException => println("cerror", e)
    case e =>
      println(e)
      e.printStackTrace
  } void
}
