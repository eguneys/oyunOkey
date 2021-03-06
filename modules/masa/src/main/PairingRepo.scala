package oyun.masa

import org.joda.time.DateTime
import reactivemongo.bson._

import BSONHandlers._
import oyun.db.dsl._

import okey.Side
import oyun.game.Game

object PairingRepo {

  private lazy val coll = Env.current.pairingColl

  private def selectId(id: String) = $doc("_id" -> id)
  def selectMasa(masaId: String) = $doc("mid" -> masaId)
  def selectSeat(seatId: String) = $doc("$or" -> List(
    $doc("sids.e" -> seatId),
    $doc("sids.w" -> seatId),
    $doc("sids.n" -> seatId),
    $doc("sids.s" -> seatId)))

  private def selectMasaSeat(masaId: String, seatId: Player.SeatID) = selectMasa(masaId) ++ selectSeat(seatId)

  private val selectPlaying = $doc("s" -> $doc("$lt" -> okey.Status.Aborted.id))
  //private val selectPlaying = $doc("s" -> $doc("$lt" -> okey.Status.MiddleEnd.id))
  //private val selectFinished = $doc("s" -> $doc("$gte" -> okey.Status.NormalEnd.id))
  private val selectFinished = $doc("ec" -> true)

  private val recentSort = $doc("d" -> -1)
  private val chronoSort = $doc("d" -> 1)

  def byId(id: String): Fu[Option[Pairing]] = coll.find(selectId(id)).uno[Pairing]

  def recentByMasa(masaId: String, nb: Int): Fu[Pairings] =
    coll.find(selectMasa(masaId)).sort(recentSort).cursor[Pairing]().gather[List](nb)

  def removeByMasa(masaId: String) = coll.remove(selectMasa(masaId)).void

  def count(masaId: String): Fu[Int] =
    coll.count(selectMasa(masaId).some)

  def countFinished(masaId: String): Fu[Int] =
    coll.count((selectMasa(masaId) ++ selectFinished).some)

  def findPlaying(masaId: String): Fu[Option[Pairing]] = coll.find(selectMasa(masaId) ++ selectPlaying).uno[Pairing]

  def removePlaying(masaId: String) = coll.remove(selectMasa(masaId) ++ selectPlaying).void

  def finishedBySeatChronological(masaId: String, seatId: String): Fu[Pairings] =
    coll.find(
      selectMasaSeat(masaId, seatId) ++ selectFinished
    ).sort(chronoSort).cursor[Pairing]().gather[List]()

  def insert(pairing: Pairing) = coll.insert {
    pairingHandler.write(pairing) ++ $doc("d" -> DateTime.now)
  }.void

  def finish(g: oyun.game.Game) = coll.update(
    selectId(g.id),
    $doc("$set" -> $doc(
      "ec" -> g.finishedCounts,
      "s" -> g.status.id,
      "ss" -> g.endScores.map(_.map(_.total)),
      "w" -> g.winnerSide.flatMap(g.player(_).playerId),
      "t" -> g.turns))).void

  def playingByMasaAndSeatId(masaId: Masa.ID, seatId: Player.SeatID): Fu[Option[Game.ID]] = coll.find(
    selectMasaSeat(masaId, seatId) ++ selectPlaying,
    $doc("_id" -> true)
  ).sort(recentSort).uno[Bdoc].map {
    _.flatMap(_.getAs[Game.ID]("_id"))
  }

  // def playingPlayerIds(masa: Masa): Fu[Set[String]] =
  //   coll.find(selectMasa(masa.id) ++ selectPlaying).one[Pairing] map {
  //     _ map { _.playerIds.toSet } getOrElse Set.empty
  //   }
  def playingSeatIds(masa: Masa): Fu[Set[String]] =
    coll.find(selectMasa(masa.id) ++ selectPlaying).one[Pairing] map {
      _ map { _.seatIds.toSet } getOrElse Set.empty
    }
}
