package oyun.masa

import reactivemongo.bson._
import reactivemongo.core.commands._

import okey.Side

import BSONHandlers._
import oyun.db.dsl._

object PlayerRepo {
  private lazy val coll = Env.current.playerColl

  private def selectId(id: String) = $doc("_id" -> id)
  private def selectMasa(masaId: String) = $doc("mid" -> masaId)
  private def selectMasaPlayer(masaId: String, playerId: String) = $doc(
    "mid" -> masaId,
    "_id" -> playerId)

  private def selectMasaUser(masaId: String, userId: String) = $doc(
    "mid" -> masaId,
    "uid" -> userId)

  private val selectActive = $doc("a" -> $doc("$eq" -> true))
  private def selectSide(side: Side) = $doc("d" -> side.letter.toString)
  private def selectActiveSide(side: Side) = $doc(
    "d" -> side.letter.toString,
    "a" -> true)

  private def selectUser = $doc("uid" -> $doc("$exists" -> true))

  private val bestSort = $doc("m" -> -1)

  def byId(id: String): Fu[Option[Player]] = coll.uno[Player](selectId(id))

  def bestByMasa(masaId: String): Fu[List[Player]] =
    coll.find(selectMasa(masaId)).sort(bestSort).cursor[Player]().gather[List]()

  def bestByMasaWithRank(masaId: String): Fu[RankedPlayers] =
    bestByMasa(masaId).map { res =>
      res.foldRight(List.empty[RankedPlayer] -> (res.size)) {
        case (p, (res, rank)) => (RankedPlayer(rank, p) :: res, rank - 1)
      }._1
    }

  def find(masaId: String, playerId: String): Fu[Option[Player]] =
    coll.find(selectMasaPlayer(masaId, playerId)).uno[Player]

  def update(masaId: String, playerId: String)(f: Player => Fu[Player]) =
    find(masaId, playerId) flatten s"No such player: $masaId/$playerId" flatMap f flatMap { player =>
      coll.update(selectId(player._id), player).void
    }

  def findByUserId(masaId: String, userId: String): Fu[Option[Player]] =
    coll.find(selectMasaUser(masaId, userId)).uno[Player]

  def activeSide(masaId: String, side: Side): Fu[Option[Player]] =
    coll.find(selectMasa(masaId) ++ selectActiveSide(side)).uno[Player]

  def countActive(masaId: String): Fu[Int] =
    coll.count(Some(selectMasa(masaId) ++ selectActive))

  def removeByMasa(masaId: String) = coll.remove(selectMasa(masaId)).void

  def remove(masaId: String, playerId: String) =
    coll.remove(selectMasaPlayer(masaId, playerId)).void

  def join(masaId: String, player: Player, oside: Option[Side]) =
    freeSides(masaId) flatMap { l =>
      l.find(s => (oside | s) == s) match {
        case Some(side) =>
          find(masaId, player.id) flatMap {
            case Some(p) =>
              coll.update(selectId(p._id),
                $doc("$set" -> selectActiveSide(side)))
            case None => coll.insert(player.doActiveSide(side))
          } void
        case None => funit
      }
    }

  def withdraw(masaId: String, playerId: String) = coll.update(
    selectMasaPlayer(masaId, playerId),
    $doc("$set" -> $doc("a" -> false))).void

  def activePlayerIds(masaId: String): Fu[List[String]] =
    activePlayers(masaId) map { _ map (_.id) }

  def winner(masaId: String): Fu[Option[Player]] =
    coll.find(selectMasa(masaId)).sort(bestSort).uno[Player]

  def activePlayers(masaId: String): Fu[List[Player]] =
    coll.find(selectMasa(masaId) ++ selectActive).cursor[Player]().gather[List]()

  def freeSides(masaId: String): Fu[List[Side]] =
    activePlayers(masaId) map { l => Side.all filterNot (l map (_.side) toSet) }

  def allByMasa(masaId: String): Fu[List[Player]] =
    coll.find(selectMasa(masaId)).cursor[Player]().gather[List]()


  def allUserPlayers(masaId: String): Fu[List[Player]] =
    coll.find(selectMasa(masaId) ++ selectUser).cursor[Player]().gather[List]()

  def playerInfo(masaId: String, playerId: String): Fu[Option[PlayerInfo]] = find(masaId, playerId) map {
    _ map { player =>
      PlayerInfo(player.side, player.active)
    }
  }
}
