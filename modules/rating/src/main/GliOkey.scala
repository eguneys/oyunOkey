package oyun.rating

import reactivemongo.bson.BSONDocument

import oyun.db.BSON

case class GliOkey(
  rating: Double) {
  def intRating = rating.toInt

  def sanityCheck =
    rating > 0 &&
      rating < 4000
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

  sealed abstract class Result(val v: Int) {

  }

  object Result {
    def apply(v: Int) = v match {
      case 1 => First
      case 2 => Second
      case 3 => Third
      case 4 => Fourth
    }

    case object First extends Result(1)
    case object Second extends Result(2)
    case object Third extends Result(3)
    case object Fourth extends Result(4)
  }
}
