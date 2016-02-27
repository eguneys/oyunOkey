package oyun.common

import scala.concurrent.duration._

import akka.actor._
import akka.pattern.{ ask }

import ornicar.scalalib.Random.{ approximatly }

final class Scheduler(scheduler: akka.actor.Scheduler, enabled: Boolean, debug: Boolean) {

  def message(freq: FiniteDuration)(to: => (ActorRef, Any)) {
    enabled ! scheduler.schedule(freq, randomize(freq), to._1, to._2)
  }

  def once(delay: FiniteDuration)(op: => Unit) {
    enabled ! scheduler.scheduleOnce(delay)(op)
  }

  private def randomize(d: FiniteDuration, ratio: Float = 0.05f): FiniteDuration =
    approximatly(ratio)(d.toMillis) millis
}
