package oyun.rating

object Regulator {

  def apply(perfType: PerfType, before: Perf, after: Perf) =
    if (before.nb >= after.nb) after
    else {
      val diff = (after.gliokey.rating - before.gliokey.rating).abs
      val extra = 0
      after.copy(
        gliokey = after.gliokey.copy(
          rating = after.gliokey.rating + extra))
    }

}
