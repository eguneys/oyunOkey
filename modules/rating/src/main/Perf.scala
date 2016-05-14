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

  def isEmpty = nb == 0
  def nonEmpty = !isEmpty

}


case object Perf {

  type Key = String
  type ID = Int

  val default = Perf(GliOkey.default, 0, Nil, None)

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
