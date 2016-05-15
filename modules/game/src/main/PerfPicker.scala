package oyun.game

import oyun.rating.{ Perf, PerfType }
import oyun.user.Perfs

object PerfPicker {

  val default = (perfs: Perfs) => perfs.yuzbir

  def main(variant: okey.variant.Variant): Option[Perfs => Perf] =
    Some {
      (perfs: Perfs) => perfs.yuzbir
    }

  def main(game: Game): Option[Perfs => Perf] = main(game.ratingVariant)
}
