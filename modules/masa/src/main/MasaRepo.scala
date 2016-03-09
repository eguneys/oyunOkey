package oyun.masa

import reactivemongo.bson.{ BSONDocument, BSONArray }

import BSONHandlers._

object MasaRepo {
  private lazy val coll = Env.current.masaColl

  private def selectId(id: String) = BSONDocument("_id" -> id)

  def byId(id: String): Fu[Option[Masa]] = coll.find(selectId(id)).one[Masa]

  def insert(masa: Masa) = coll.insert(masa)

  def exists(id: String) = coll.count(BSONDocument("_id" -> id).some) map (0 != )
}
