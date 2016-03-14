package oyun.masa

import reactivemongo.bson.{ BSONDocument, BSONArray }

import oyun.db.Implicits._
import BSONHandlers._

object MasaRepo {
  private lazy val coll = Env.current.masaColl

  private def selectId(id: String) = BSONDocument("_id" -> id)

  private val enterableSelect = BSONDocument(
    "status" -> BSONDocument("$in" -> List(Status.Created.id))
  )

  private val createdSelect = BSONDocument("status" -> Status.Created.id)
  private val startedSelect = BSONDocument("status" -> Status.Started.id)

  def byId(id: String): Fu[Option[Masa]] = coll.find(selectId(id)).one[Masa]

  def createdById(id: String): Fu[Option[Masa]] =
    coll.find(selectId(id) ++ createdSelect).one[Masa]

  def enterableById(id: String): Fu[Option[Masa]] =
    coll.find(selectId(id)).one[Masa]

  def startedById(id: String): Fu[Option[Masa]] =
    coll.find(selectId(id) ++ startedSelect).one[Masa]

  def started: Fu[List[Masa]] =
    coll.find(startedSelect).toList[Masa](None)


  def setStatus(masaId: String, status: Status) = coll.update(
    selectId(masaId),
    BSONDocument("$set" -> BSONDocument("status" -> status.id))
  ).void

  def insert(masa: Masa) = coll.insert(masa)

  def exists(id: String) = coll.count(BSONDocument("_id" -> id).some) map (0 != )

  private def allCreatedSelect = createdSelect

  def allCreated: Fu[List[Masa]] =
    coll.find(allCreatedSelect).cursor[Masa]().collect[List]()

}
