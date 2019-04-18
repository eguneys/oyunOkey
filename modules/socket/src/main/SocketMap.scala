package oyun.socket

import scala.concurrent.duration._
import ornicar.scalalib.Random.approximatly

import oyun.hub.{ Trouper, TrouperMap }

object SocketMap {

  def apply[T <: Trouper](
    system: akka.actor.ActorSystem,
    mkTrouper: String => T,
    accessTimeout: FiniteDuration,
    monitoringName: String,
    broomFrequency: FiniteDuration): TrouperMap[T] = {

    val trouperMap = new TrouperMap[T](
      mkTrouper = mkTrouper,
      accessTimeout = accessTimeout)

    system.scheduler.schedule(approximatly(0.1f)(12.seconds.toMillis).millis, broomFrequency) {
      trouperMap tellAll actorApi.Broom
    }

    trouperMap
  }
}
