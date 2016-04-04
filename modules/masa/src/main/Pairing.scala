package oyun.masa

import okey.{ Sides, Side }
import oyun.game.{ IdGenerator }

case class Pairing(
  id: String, // game id
  masaId: String,
  status: okey.Status,
  playerIds: Sides[String],
  scores: Sides[Int]) {

  def gameId = id

  def scoreOf(playerId: String): Option[Int] = (playerIds zip scores find {
    _._1 == playerId
  }) map (_._2)
}

private[masa] object Pairing {
  def apply(masaId: String, playerIds: Sides[String]): Pairing = new Pairing(
    id = IdGenerator.game,
    masaId = masaId,
    status = okey.Status.Created,
    playerIds = playerIds,
    scores = Sides[Int])

  case class Prep(masaId: String, player1: String, player2: String, player3: String, player4: String) {
    def toPairing =
      Pairing(masaId, Sides(player1, player2, player3, player4))
  }

  def prep(masa: Masa, p1: String, p2: String, p3: String, p4: String) = Pairing.Prep(masa.id, p1, p2, p3, p4)
}
