package oyun.masa

import play.api.libs.json._

final class JsonView() {

  private case class CachableData(
    pairings: JsArray,
    actives: JsArray)

  def apply(masa: Masa,
    me: Option[String],
    socketVersion: Option[Int]): Fu[JsObject] = for {
    data <- cachableData(masa.id)
  } yield Json.obj(
    "id" -> masa.id,
    "fullName" -> masa.fullName,
    "actives" -> data.actives,
    "pairings" -> data.pairings,
    "socketVersion" -> socketVersion
  )


  private val cachableData = ((id: String) => for {
    pairings <- PairingRepo.recentByMasa(id, 40)
    actives <- fuccess(Nil)
  } yield CachableData(
    pairings = JsArray(pairings map pairingJson),
    actives = JsArray(actives map playerJson)
  ))

  private def playerJson(player: Player) = Json.obj("side" -> player.side.name, "id" -> player.id)

  private def pairingUserJson(playerId: String) = playerId

  private def pairingJson(p: Pairing) = Json.obj(
    "id" -> p.gameId,
    "u" -> Json.arr(p.playerIds.map(oi => pairingUserJson(oi)))
  )
}
