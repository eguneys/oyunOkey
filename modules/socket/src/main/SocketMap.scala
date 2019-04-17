package oyun.socket

import scala.concurrent.duration._
import ornicar.scalalib.Random.approximatly

import oyun.hub.{ Trouper, TrouperMap }

object SocketMap {

  def apply[T <: Trouper](
    system: akka.actor.ActorSystem,
    mkTrouper: String => T,
    accessTimeout: FiniteDuration,
    monitoringName: String): TrouperMap[T] = {

    val trouperMap = new TrouperMap[T](
      mkTrouper = mkTrouper,
      accessTimeout = accessTimeout)

    trouperMap    
  }
}
