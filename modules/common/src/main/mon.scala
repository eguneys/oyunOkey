package oyun

import kamon.Kamon.{ metrics }

object mon {
  object masa {
    object pairing {
      val create = inc("masa.pairing.create")
    }
    val created = rec("masa.created")
    val started = rec("masa.started")
    val player = rec("masa.player")
  }

  type Rec = Long => Unit
  type Inc = () => Unit

  private def inc(name: String): Inc = metrics.counter(name).increment _

  private def rec(name: String): Rec = {
    val hist = metrics.histogram(name)
    value => {
      if (value < 0) logger.warn(s"Negative histogram value: $name=$value")
      else hist.record(value)
    }
  }

  private val logger = oyun.log("monitor")
}
