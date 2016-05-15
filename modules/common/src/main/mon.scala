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

  object round {
    object error {
      val gliokey = inc("round.error.gliokey")
    }

    object titivate {
      val game = rec("round.titivate.game") // how many games were processed
      val total = rec("round.titivate.total") // how many games should have been processed
      val old = rec("round.titivate.old") // how many old games remain
    }
  }

  type Rec = Long => Unit
  type Inc = () => Unit

  type IncPath = oyun.mon.type => Inc

  def incPath(f: oyun.mon.type => Inc): Inc = f(this)

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
