package oyun.user

import reactivemongo.bson.BSONDocument

import oyun.db.BSON
import oyun.rating.{ Perf, PerfType }

case class Perfs(
  yuzbir: Perf) {

  def perfs = List(
    "yuzbir" -> yuzbir)

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
