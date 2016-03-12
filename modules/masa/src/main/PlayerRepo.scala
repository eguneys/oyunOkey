package oyun.masa

import reactivemongo.bson._

import okey.Side

import BSONHandlers._

object PlayerRepo {
  private lazy val coll = Env.current.playerColl

  private def selectId(id: String) = BSONDocument("_id" -> id)
  private def selectMasa(masaId: String) = BSONDocument("mid" -> masaId)
  private def selectMasaPlayer(masaId: String, playerId: String) = BSONDocument(
    "mid" -> masaId,
    "_id" -> playerId)
  private val selectActive = BSONDocument("a" -> BSONDocument("$eq" -> true))
  private def selectSide(side: Side) = BSONDocument("s" -> side.letter.toString)
  private def selectActiveSide(side: Side) = BSONDocument(
    "s" -> side.letter.toString,
    "a" -> true)

  def find(masaId: String, playerId: String): Fu[Option[Player]] =
    coll.find(selectMasaPlayer(masaId, playerId)).one[Player]

  def findByUserId(masaId: String, userId: String): Fu[Option[Player]] =
    fuccess(None)

  def activeSide(masaId: String, side: Side): Fu[Option[Player]] =
    coll.find(selectMasa(masaId) ++ selectActiveSide(side)).one[Player]

  def join(masaId: String, player: Player, oside: Option[Side]) =
    freeSides(masaId) flatMap { l =>
      l.find(s => (oside | s) == s) match {
        case Some(side) =>
          find(masaId, player.id) flatMap {
            case Some(p) => coll.update(selectId(p._id),
              BSONDocument("$set" -> selectActiveSide(side)))
            case None => coll.insert(player.doActiveSide(side))
          } void
        case None => funit
      }
    }

  def withdraw(masaId: String, playerId: String) = coll.update(
    selectMasaPlayer(masaId, playerId),
    BSONDocument("$set" -> BSONDocument("a" -> false))).void

  def activePlayers(masaId: String): Fu[List[Player]] =
    coll.find(selectMasa(masaId) ++ selectActive).cursor[Player]().collect[List]()

  def freeSides(masaId: String): Fu[List[Side]] =
    activePlayers(masaId) map { l => Side.all filterNot (l map (_.side) toSet) }


  def playerInfo(masaId: String, playerId: String): Fu[Option[PlayerInfo]] = find(masaId, playerId) map {
    _ map { player =>
      PlayerInfo(player.side, player.active)
    }
  }
}
