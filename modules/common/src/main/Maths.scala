package oyun.common

import scala.math.{ pow }

object Maths {

  def truncateAt(n: Double, p: Int): Double = {
    val s = math.pow(10, p)
    (math floor n * s) / s
  }
}
