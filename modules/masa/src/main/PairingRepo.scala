package oyun.masa

import org.joda.time.DateTime
import reactivemongo.bson._

import BSONHandlers._
import oyun.db.BSON._

import okey.Side

object PairingRepo {

  private lazy val coll = Env.current.pairingColl

  private def selectId(id: String) = BSONDocument("_id" -> id)
  def selectMasa(masaId: String) = BSONDocument("mid" -> masaId)
  def selectPlayer(playerId: String) = BSONDocument("$or" -> List(
    BSONDocument("pids.e" -> playerId),
    BSONDocument("pids.w" -> playerId),
    BSONDocument("pids.n" -> playerId),
    BSONDocument("pids.s" -> playerId)))

  private def selectMasaPlayer(masaId: String, playerId: String) = selectMasa(masaId) ++ selectPlayer(playerId)

  private val selectPlaying = BSONDocument("s" -> BSONDocument("$lt" -> okey.Status.End.id))
  private val selectFinished = BSONDocument("s" -> BSONDocument("$gte" -> okey.Status.End.id))
  private val recentSort = BSONDocument("d" -> -1)
  private val chronoSort = BSONDocument("d" -> 1)

  def recentByMasa(masaId: String, nb: Int): Fu[Pairings] =
    coll.find(selectMasa(masaId)).sort(recentSort).cursor[Pairing]().collect[List](nb)


  def finishedByPlayerChronological(masaId: String, playerId: String): Fu[Pairings] =
    coll.find(
      selectMasaPlayer(masaId, playerId) ++ selectFinished
    ).sort(chronoSort).cursor[Pairing]().collect[List]()

  def insert(pairing: Pairing) = coll.insert {
    pairingHandler.write(pairing) ++ BSONDocument("d" -> DateTime.now)
  }.void

  def finish(g: oyun.game.Game) = coll.update(
    selectId(g.id),
    BSONDocument("$set" -> BSONDocument(
      "s" -> g.status.id,
      "ss" -> g.endScores.map(_.map(_.total)),
      "t" -> g.turns))).void

  def playingPlayerIds(masa: Masa): Fu[Set[String]] =
    coll.find(selectMasa(masa.id) ++ selectPlaying).one[Pairing] map {
      _ map { _.playerIds.toSet } getOrElse Set.empty
    }
}
