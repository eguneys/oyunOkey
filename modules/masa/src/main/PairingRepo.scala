package oyun.masa

import reactivemongo.bson._

import BSONHandlers._

import okey.Side

object PairingRepo {

  private lazy val coll = Env.current.pairingColl

  private def selectId(id: String) = BSONDocument("_id" -> id)
  def selectMasa(masaId: String) = BSONDocument("mid" -> masaId)
  private val recentSort = BSONDocument("d" -> -1)

  def recentByMasa(masaId: String, nb: Int): Fu[Pairings] =
    coll.find(selectMasa(masaId)).sort(recentSort).cursor[Pairing]().collect[List](nb)


  def join(id: String, playerId: String, side: Side) = coll.update(
    selectId(id),
    BSONDocument("$set" -> BSONDocument(
      s"pids.${side.letter}" ->  playerId
    ))).void
}
