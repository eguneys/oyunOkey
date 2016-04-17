package oyun.masa

import BSONHandlers._
import oyun.db.dsl._

object MasaRepo {
  private lazy val coll = Env.current.masaColl

  private def $id(id: String) = $doc("_id" -> id)

  private val enterableSelect = $doc(
    "status" $in (Status.Created.id))

  private val createdSelect = $doc("status" -> Status.Created.id)
  private val startedSelect = $doc("status" -> Status.Started.id)
  private[masa] val finishedSelect = $doc("status" -> Status.Finished.id)

  def byId(id: String): Fu[Option[Masa]] = coll.find($id(id)).uno[Masa]

  def createdById(id: String): Fu[Option[Masa]] =
    coll.find($id(id) ++ createdSelect).uno[Masa]

  def enterableById(id: String): Fu[Option[Masa]] =
    coll.find($id(id)).uno[Masa]

  def startedById(id: String): Fu[Option[Masa]] =
    coll.find($id(id) ++ startedSelect).uno[Masa]

  def finishedById(id: String): Fu[Option[Masa]] =
    coll.find($id(id) ++ finishedSelect).uno[Masa]

  def started: Fu[List[Masa]] =
    coll.find(startedSelect).sort($doc("createdAt" -> -1)).list[Masa](None)

  def publicStarted: Fu[List[Masa]] =
    coll.find(startedSelect)
      .sort($doc("createdAt" -> -1))
      .list[Masa]()

  def finished(limit: Int): Fu[List[Masa]] =
    coll.find(finishedSelect)
      .sort($doc("createdAt" -> -1))
      .list[Masa](limit)

  def finishedNotable(limit: Int): Fu[List[Masa]] =
    coll.find(finishedSelect)
      .sort($doc("createdAt" -> -1))
      .list[Masa](limit)

  def setStatus(masaId: String, status: Status) = coll.update(
    $id(masaId),
    $doc("$set" -> $doc("status" -> status.id))
  ).void

  def setNbPlayers(masaId: String, nb: Int) = coll.update(
    $id(masaId),
    $doc("$set" -> $doc("nbPlayers" -> nb))
  ).void

  def setNbRounds(masaId: String, nb: Int) = coll.update(
    $id(masaId),
    $doc("$set" -> $doc("nbRounds" -> nb))
  ).void

  def insert(masa: Masa) = coll.insert(masa)

  def exists(id: String) = coll.count($doc("_id" -> id).some) map (0 != )

  private def allCreatedSelect = createdSelect

  def publicCreatedSorted: Fu[List[Masa]] =
    coll.find(allCreatedSelect).list[Masa](none)

  def allCreated: Fu[List[Masa]] =
    coll.find(allCreatedSelect).cursor[Masa]().gather[List]()


  def promotable: Fu[List[Masa]] =
    publicCreatedSorted map {
      case created => created
    }
}
