package oyun.rating

import org.joda.time.DateTime
import reactivemongo.bson.BSONDocument

import oyun.db.BSON

case class Perf(
  gliokey: GliOkey,
  nb: Int,
  recent: List[Int],
  latest: Option[DateTime]) {

  def intRating = gliokey.rating.toInt

  def progress: Int = ~recent.headOption.flatMap { head =>
    recent.lastOption map (head-)
  }

  def add(g: GliOkey, date: DateTime): Perf = copy(
    gliokey = g,
    nb = nb + 1,
    recent =
      if (nb < 10) recent
      else (g.intRating :: recent) take Perf.recentMaxSize,
    latest = date.some)

  def add(r: Rating, date: DateTime): Option[Perf] = {
    val gliokey = GliOkey(r.rating)
    gliokey.sanityCheck option add(gliokey, date)
  }

  def addOrReset(monitor: oyun.mon.IncPath, msg: => String)(r: Rating, date: DateTime): Perf = add(r, date) | {
    oyun.log("rating").error(s"Crazy GliOkey $msg")
    oyun.mon.incPath(monitor)()
    add(GliOkey.default, date)
  }

  def toRating = new Rating(
    math.max(GliOkey.minRating, gliokey.rating),
    nb)

  def isEmpty = nb == 0
  def nonEmpty = !isEmpty
}


case object Perf {

  type Key = String
  type ID = Int

  val default = Perf(GliOkey.default, 0, Nil, None)

  val recentMaxSize = 12

  implicit val perfBSONHandler = new BSON[Perf] {
    def reads(r: BSON.Reader): Perf = Perf(
      gliokey = r.getO[GliOkey]("gl") | GliOkey.default,
      nb = r intD "nb",
      latest = r dateO "la",
      recent = r intsD "re")

    def writes(w: BSON.Writer, o: Perf) = BSONDocument(
      "gl" -> o.gliokey,
      "nb" -> w.int(o.nb),
      "re" -> w.listO(o.recent),
      "la" -> o.latest.map(w.date)
    )
  }

}
