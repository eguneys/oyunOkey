package oyun.round

import akka.actor._
import org.joda.time.DateTime
import play.api.libs.iteratee._
import scala.concurrent.duration._

import oyun.game.{ Query, Game, GameRepo }
import oyun.hub.actorApi.map.Tell
import oyun.round.actorApi.round.{ Abandon }

private[round] final class Titivate(
  roundMap: ActorRef
) extends Actor {

  object Schedule
  object Run

  override def preStart() {
    scheduleNext
    context setReceiveTimeout 30.seconds
  }

  def scheduler = context.system.scheduler

  def scheduleNext = scheduler.scheduleOnce(10 seconds, self, Run)

  def receive = {

    case ReceiveTimeout =>
      val msg = "Titivate timed out!"
      logger.error(msg)
      throw new RuntimeException(msg)

    case Run => GameRepo.count(_.checkable).flatMap { total =>
      GameRepo.cursor(Query.checkable)
        .enumerate(5000, stopOnError = false)
        .|>>>(Iteratee.foldM[Game, Int](0) {
          case (count, game) => {

            println(s"titivate ${game.id} up? ${game.unplayed} ab? ${game.abandoned}")

            if (game.finished) {
              GameRepo unsetCheckAt game
            }

            else if (game.abandoned) fuccess {
              roundMap ! Tell(game.id, Abandon)
            }

            else if (game.unplayed) {
              GameRepo remove game.id
            }

            else {
              GameRepo.setCheckAt(game, DateTime.now plusSeconds 60)
            }
          } inject (count + 1)
        })
        .addEffect { count =>
          oyun.mon.round.titivate.game(count)
          oyun.mon.round.titivate.total(total)
        }.>> {
          GameRepo.count(_.checkableOld).map(oyun.mon.round.titivate.old(_))
        }
        .andThenAnyway(scheduleNext)
    }
  }
}
