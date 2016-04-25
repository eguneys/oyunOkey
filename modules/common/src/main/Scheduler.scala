package oyun.common

import scala.concurrent.duration._

import akka.actor._
import akka.pattern.{ ask }

import ornicar.scalalib.Random.{ approximatly, nextString }

final class Scheduler(scheduler: akka.actor.Scheduler, enabled: Boolean, debug: Boolean) {

  def message(freq: FiniteDuration)(to: => (ActorRef, Any)) {
    enabled ! scheduler.schedule(freq, randomize(freq), to._1, to._2)
  }

  def effect(freq: FiniteDuration, name: String)(op: => Unit) {
    enabled ! future(freq, name)(fuccess(op))
  }

  def future(freq: FiniteDuration, name: String)(op: => Funit) {
    enabled ! {
      val f = randomize(freq)
      val doDebug = debug && freq > 5.seconds
      logger.info("schedule %s every %s".format(name, freq))
      scheduler.schedule(f, f) {
        val tagged = "(%s) %s".format(nextString(3), name)
        doDebug ! logger.info(tagged)
        val start = nowMillis
        op effectFold (
          e => logger.error("(%s) %s".format(tagged, e.getMessage), e),
          _ => doDebug ! logger.info(tagged + " - %d ms".format(nowMillis - start))
        )
      }
    }
  }

  def once(delay: FiniteDuration)(op: => Unit) {
    enabled ! scheduler.scheduleOnce(delay)(op)
  }

  private def logger = oyun.log("scheduler")

  private def randomize(d: FiniteDuration, ratio: Float = 0.05f): FiniteDuration =
    approximatly(ratio)(d.toMillis) millis
}
