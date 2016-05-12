package oyun.user

import oyun.db.BSON
import reactivemongo.bson.BSONDocument

case class Count(
  ai: Int,
  game: Int,
  rated: Int,
  standing1: Int,
  standing2: Int,
  standing3: Int,
  standing4: Int)


object Count {

  private[user] val countBSONHandler = new BSON[Count] {

    def reads(r: BSON.Reader): Count = Count(
      ai = r nInt "ai",
      game = r nInt "game",
      rated = r nInt "rated",
      standing1 = r nInt "standing1",
      standing2 = r nInt "standing2",
      standing3 = r nInt "standing3",
      standing4 = r nInt "standing4")

    def writes(w: BSON.Writer, o: Count) = BSONDocument(
      "ai" -> w.int(o.ai),
      "game" -> w.int(o.game),
      "rated" -> w.int(o.rated),
      "standing1" -> w.int(o.standing1),
      "standing2" -> w.int(o.standing2),
      "standing3" -> w.int(o.standing3),
      "standing4" -> w.int(o.standing4))
  }

  val default = Count(0, 0, 0, 0, 0, 0, 0)
}
