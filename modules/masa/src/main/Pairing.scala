package oyun.masa

import okey.{ Sides, Side }
import oyun.game.{ IdGenerator }

case class Pairing(
  id: String, // game id
  masaId: String,
  playerIds: Sides[String]) {

  def gameId = id
}

private[masa] object Pairing {
  def apply(masaId: String, playerIds: Sides[String]): Pairing = new Pairing(
    id = IdGenerator.game,
    masaId = masaId,
    playerIds = playerIds)

}
