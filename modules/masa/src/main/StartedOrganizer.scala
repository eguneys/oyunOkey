package oyun.masa

import akka.actor._
import scala.concurrent.duration._

import actorApi._

private[masa] final class StartedOrganizer(
  api: MasaApi,
  isOnline: String => Player => Fu[Boolean],
  socketHub: ActorRef) extends Actor {

  override def preStart {
    pairingLogger.info("Start StartedOrganizer")
    context setReceiveTimeout 15.seconds
    scheduleNext
  }

  case object Tick

  def scheduleNext =
    context.system.scheduler.scheduleOnce(3 seconds, self, Tick)

  def receive = {
    case ReceiveTimeout =>
      val msg = "masa.StartedOrganizer timed out!"
      pairingLogger.error(msg)
      throw new RuntimeException(msg)

    case Tick =>
      val myself = self
      MasaRepo.started map { started =>
        oyun.common.Future.traverseSequentially(started) { masa =>
          PlayerRepo activePlayerIds masa.id flatMap { activePlayerIds =>
            val nb = activePlayerIds.size

            val result: Funit =
              if (masa.roundsToFinish == 0) {
                // println("masa finish", masa.rounds, masa.nbRounds)
                fuccess(api finish masa)
              }
              else if (!masa.isAlmostFinished) startPairing(masa, activePlayerIds)
              else funit
            result >>- {
            } inject nb
          }
        }.addEffect { playerCounts =>
          oyun.mon.masa.player(playerCounts.sum)
          oyun.mon.masa.started(started.size)
        }
      } andThenAnyway scheduleNext
  }

  private def startPairing(masa: Masa, activePlayerIds: List[String]): Funit = {
    fuccess(activePlayerIds) zip PairingRepo.playingPlayerIds(masa) map {
      case (activePlayers, playingUsers) =>
        val users = activePlayerIds filter { k => !playingUsers.contains(k) }
        users.headOption map { _ => pairingLogger.debug(s"start ${masa.id}") }
        api.makePairings(masa, users)
    }
  }
}
