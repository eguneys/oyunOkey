package oyun.fishnet

import scala.concurrent.duration._

import akka.actor._

private[fishnet] class ControllerActor(
  repo: FishnetRepo,
  api: FishnetApi) extends Actor {

  override def preStart {
    context setReceiveTimeout 15.seconds
    scheduleNext
  }

  lazy val offlineClient = Client.offline

  def scheduleNext =
    context.system.scheduler.scheduleOnce(2 seconds, self, Acquire(offlineClient))

  case class Acquire(client: Client)

  def receive = {
    case Acquire(client) => {
      api acquireMove client map { _ ?? { workMove =>
        val move = findMove(workMove.game.game)
        println("fishnet acquired move", move)
        api.postMove(workMove.id, client, move)
      }
      }
    } andThenAnyway scheduleNext
  }

  private def findMove(game: oyun.game.Game): Option[okey.format.Uci] = {
    import okey.{ DrawMiddle, Discard, LeaveTaken, CollectOpen }

    val okeyGame = game.toOkey

    val board = okeyGame.table.boards(okeyGame.player.side)
    return (okeyGame.situation.actions.foldLeft(none[okey.Action]) {
      case (found@Some(_), _) => found
      case (_, DrawMiddle) => DrawMiddle.some
      case (_, Discard) => board.pieceList.headOption map Discard.apply
      case (_, LeaveTaken) => LeaveTaken.some
      case (_, CollectOpen) => CollectOpen.some
      case _ => None
    }).map (_.toUci)
  }

}
