package oyun.game

import oyun.rating.{ Perf, PerfType }
import oyun.user.Perfs

object PerfPicker {

  val default = (perfs: Perfs) => perfs.yuzbir

  def perfType(variant: okey.variant.Variant): Option[PerfType] =
    PerfType(key(variant))

  def key(variant: okey.variant.Variant): String =
    variant.key

  def main(variant: okey.variant.Variant): Option[Perfs => Perf] =
    Some {
      (perfs: Perfs) => perfs.yuzbir
    }

  def main(game: Game): Option[Perfs => Perf] = main(game.ratingVariant)

  def mainOrDefault(game: Game): Perfs => Perf = mainOrDefault(game.ratingVariant)

  def mainOrDefault(variant: okey.variant.Variant): Perfs => Perf =
    main(variant) getOrElse default
}
