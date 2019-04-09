package oyun.common

object Chronometer {

  case class Lap[A](result: A, nanos: Long) {
    def millis = (nanos / 1000000).toInt
    def micros = (nanos / 1000).toInt

    def showDuration: String = if (millis >= 1) f"$millis%.2f ms" else s"$micros micros"
  }

  def sync[A](f: => A): Lap[A] = {
    val start = nowNanos
    val res = f
    Lap(res, nowNanos - start)
  }

  def syncEffect[A](f: => A)(effect: Lap[A] => Unit): A = {
    val lap = sync(f)
    effect(lap)
    lap.result
  }
}
