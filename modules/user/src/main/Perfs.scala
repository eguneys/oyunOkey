package oyun.user

import reactivemongo.bson.BSONDocument

import oyun.db.BSON
import oyun.rating.{ Perf, PerfType }

case class Perfs(
  yuzbir: Perf) {

  def perfs = List(
    "yuzbir" -> yuzbir)

  def bestPerf: Option[(PerfType, Perf)] = {
    val ps = PerfType.all map { pt => pt -> apply(pt) }
    val minNb = math.max(1, ps.foldLeft(0)(_ + _._2.nb) / 10)
    ps.foldLeft(none[(PerfType, Perf)]) {
      case (ro, p) if p._2.nb >= minNb => ro.fold(p.some) { r =>
        Some(if (p._2.intRating > r._2.intRating) p else r)
      }
      case (ro, _) => ro
    }
  }

  lazy val perfsMap: Map[String, Perf] = Map(
    "yuzbir" -> yuzbir
  )

  def apply(key: String): Option[Perf] = perfsMap get key

  def apply(perfType: PerfType): Perf = perfType match {
    case PerfType.Yuzbir => yuzbir
  }
}

case object Perfs {

  val default = {
    val p = Perf.default
    Perfs(p)
  }

  val perfsBSONHandler = new BSON[Perfs] {

    implicit def perfHandler = Perf.perfBSONHandler
    import BSON.MapDocument._

    def reads(r: BSON.Reader): Perfs = {
      def perf(key: String) = r.getO[Perf](key) getOrElse Perf.default
      Perfs(
        yuzbir = perf("yuzbir")
      )
    }

    private def notNew(p: Perf): Option[Perf] = p.nb > 0 option p

    def writes(w: BSON.Writer, o: Perfs) = BSONDocument(
      "yuzbir" -> notNew(o.yuzbir)
    )
  }

  case class Leaderboards(
    yuzbir: List[User.LightPerf])

}
