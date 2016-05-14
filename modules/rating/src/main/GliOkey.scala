package oyun.rating

import reactivemongo.bson.BSONDocument

import oyun.db.BSON

case class GliOkey(
  rating: Double) {
  def intRating = rating.toInt
}

case object GliOkey {

  val minRating = 800

  val default = GliOkey(1500d)

  implicit val gliokeyBSONHandler = new BSON[GliOkey] {

    def reads(r: BSON.Reader): GliOkey = GliOkey(
      rating = r double "r")

    def writes(w: BSON.Writer, o: GliOkey) = BSONDocument(
      "r" -> w.double(o.rating))
  }
}
