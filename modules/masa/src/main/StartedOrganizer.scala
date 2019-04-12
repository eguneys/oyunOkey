package oyun.masa

import akka.actor._
import scala.concurrent.duration._

import actorApi._

private[masa] final class StartedOrganizer(
  api: MasaApi,
  reminder: ActorRef,
  isOnline: String => Player => Fu[Boolean],
  socketHub: ActorRef) extends Actor {

  override def preStart {
    pairingLogger.info("Start StartedOrganizer")
    context setReceiveTimeout 15.seconds
    scheduleNext
  }

  case object Tick

  def scheduleNext =
    context.system.scheduler.scheduleOnce(5 seconds, self, Tick)

  def receive = {
    case ReceiveTimeout =>
      val msg = "masa.StartedOrganizer timed out!"
      pairingLogger.error(msg)
      throw new RuntimeException(msg)

    case Tick =>
      val myself = self
      MasaRepo.started map { started =>
        oyun.common.Future.traverseSequentially(started) { masa =>
          PlayerRepo activePlayers masa.id flatMap { activePlayers =>
            val activeSeatIds = activePlayers map (_.id)
            val activeUserIds = activePlayers flatMap (_.userId)
            val nb = activeSeatIds.size

            val masaFinish = masa.scores.fold(
              masa.roundsToFinish.exists(0==)) { scores =>
              activePlayers map (p => p.score) exists (_<=0)
            }

            val result: Funit =
              if (masaFinish) {
                // println("masa finish", masa.rounds, masa.nbRounds)
                fuccess(api finish masa)
              }
              else if (!masa.isAlmostFinished) startPairing(masa, activeSeatIds)
              else funit
            result >>- {
              reminder ! RemindMasa(masa, activeUserIds)
            } inject nb
          }
        }.addEffect { playerCounts =>
          oyun.mon.masa.player(playerCounts.sum)
          oyun.mon.masa.started(started.size)
        }
      } andThenAnyway scheduleNext
  }

  private def startPairing(masa: Masa, activeSeatIds: List[String]): Funit = {
    fuccess(activeSeatIds) zip PairingRepo.playingSeatIds(masa) map {
      case (activePlayers, playingSeats) =>
        val seats = activeSeatIds filter { k => !playingSeats.contains(k) }
        seats.headOption map { _ => pairingLogger.debug(s"start ${masa.id}") }
        api.makePairings(masa, seats)
    }
  }
}
