package oyun.masa

import reactivemongo.bson._
import reactivemongo.core.commands._

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
  private val bestSort = BSONDocument("m" -> -1)

  def byId(id: String): Fu[Option[Player]] = coll.find(selectId(id)).one[Player]

  def bestByMasa(masaId: String): Fu[List[Player]] =
    coll.find(selectMasa(masaId)).sort(bestSort).cursor[Player]().collect[List]()

  def bestByMasaWithRank(masaId: String): Fu[RankedPlayers] =
    bestByMasa(masaId).map { res =>
      res.foldRight(List.empty[RankedPlayer] -> (res.size)) {
        case (p, (res, rank)) => (RankedPlayer(rank, p) :: res, rank - 1)
      }._1
    }

  def find(masaId: String, playerId: String): Fu[Option[Player]] =
    coll.find(selectMasaPlayer(masaId, playerId)).one[Player]

  def update(masaId: String, playerId: String)(f: Player => Fu[Player]) =
    find(masaId, playerId) flatten s"No such player: $masaId/$playerId" flatMap f flatMap { player =>
      coll.update(selectId(player._id), player).void
    }

  def findByUserId(masaId: String, userId: String): Fu[Option[Player]] =
    fuccess(None)

  def activeSide(masaId: String, side: Side): Fu[Option[Player]] =
    coll.find(selectMasa(masaId) ++ selectActiveSide(side)).one[Player]

  def countActive(masaId: String): Fu[Int] =
    coll.count(Some(selectMasa(masaId) ++ selectActive))

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

  def activePlayerIds(masaId: String): Fu[List[String]] =
    activePlayers(masaId) map { _ map (_.id) }

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
