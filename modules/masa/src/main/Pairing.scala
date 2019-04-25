package oyun.masa

import okey.{ Sides, Side }
import oyun.game.{ IdGenerator }

case class Pairing(
  id: String, // game id
  masaId: String,
  status: okey.Status,
  seatIds: Sides[String],
  round: Int,
  scores: Sides[Int],
  endCounts: Boolean,
  winner: Option[String]) {

  def gameId = id

  def finished = status >= okey.Status.MiddleEnd
  def playing = !finished

  def scoreOf(seatId: String): Option[Int] = (seatIds zip scores find {
    _._1 == seatId
  }) map (_._2)
}

private[masa] object Pairing {
  def apply(masaId: String, seatIds: Sides[String], round: Int): Pairing = new Pairing(
    id = IdGenerator.game,
    masaId = masaId,
    status = okey.Status.Created,
    seatIds = seatIds,
    round = round,
    scores = Sides[Int],
    winner = none,
    endCounts = false)

  case class Prep(masaId: String, round: Int, seat1: String, seat2: String, seat3: String, seat4: String) {

    def toPairing = {
      val sides = shiftList(List(seat1, seat2, seat3, seat4), round)
      Pairing(masaId, sides, round)
    }

    private def shiftList[A](list: List[A], by: Int) = {
      list.drop(list.size - (by % list.size)) ::: list.take(list.size - (by % list.size))
    }
  }

  def prep(masa: Masa, s1: String, s2: String, s3: String, s4: String) = Pairing.Prep(masa.id, masa.nbRounds, s1, s2, s3, s4)
}
