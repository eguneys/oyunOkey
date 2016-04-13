package oyun.masa

import org.joda.time.DateTime
import reactivemongo.bson._

import BSONHandlers._
import oyun.db.dsl._

import okey.Side

object PairingRepo {

  private lazy val coll = Env.current.pairingColl

  private def selectId(id: String) = $doc("_id" -> id)
  def selectMasa(masaId: String) = $doc("mid" -> masaId)
  def selectPlayer(playerId: String) = $doc("$or" -> List(
    $doc("pids.e" -> playerId),
    $doc("pids.w" -> playerId),
    $doc("pids.n" -> playerId),
    $doc("pids.s" -> playerId)))

  private def selectMasaPlayer(masaId: String, playerId: String) = selectMasa(masaId) ++ selectPlayer(playerId)

  private val selectPlaying = $doc("s" -> $doc("$lt" -> okey.Status.End.id))
  private val selectFinished = $doc("s" -> $doc("$gte" -> okey.Status.End.id))
  private val recentSort = $doc("d" -> -1)
  private val chronoSort = $doc("d" -> 1)

  def recentByMasa(masaId: String, nb: Int): Fu[Pairings] =
    coll.find(selectMasa(masaId)).sort(recentSort).cursor[Pairing]().collect[List](nb)


  def count(masaId: String): Fu[Int] =
    coll.count(selectMasa(masaId).some)

  def finishedByPlayerChronological(masaId: String, playerId: String): Fu[Pairings] =
    coll.find(
      selectMasaPlayer(masaId, playerId) ++ selectFinished
    ).sort(chronoSort).cursor[Pairing]().collect[List]()

  def insert(pairing: Pairing) = coll.insert {
    pairingHandler.write(pairing) ++ $doc("d" -> DateTime.now)
  }.void

  def finish(g: oyun.game.Game) = coll.update(
    selectId(g.id),
    $doc("$set" -> $doc(
      "s" -> g.status.id,
      "ss" -> g.endScores.map(_.map(_.total)),
      "t" -> g.turns))).void

  def playingPlayerIds(masa: Masa): Fu[Set[String]] =
    coll.find(selectMasa(masa.id) ++ selectPlaying).one[Pairing] map {
      _ map { _.playerIds.toSet } getOrElse Set.empty
    }
}
