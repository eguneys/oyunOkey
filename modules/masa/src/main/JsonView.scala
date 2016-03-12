package oyun.masa

import play.api.libs.json._
import oyun.common.PimpedJson._


final class JsonView() {

  private case class CachableData(
    pairings: JsArray,
    actives: JsObject)

  def apply(masa: Masa,
    me: Option[String],
    socketVersion: Option[Int]): Fu[JsObject] = for {
    data <- cachableData(masa.id)
    myInfo <- me ?? { PlayerRepo.playerInfo(masa.id, _) }
  } yield Json.obj(
    "id" -> masa.id,
    "fullName" -> masa.fullName,
    "actives" -> data.actives,
    "pairings" -> data.pairings,
    "me" -> myInfo.map(myInfoJson),
    "socketVersion" -> socketVersion
  ).noNull


  private val cachableData = ((id: String) => for {
    pairings <- PairingRepo.recentByMasa(id, 40)
    actives <- PlayerRepo.activePlayers(id)
  } yield CachableData(
    pairings = JsArray(pairings map pairingJson),
    actives = JsObject(actives map playerJson)
  ))

  private def myInfoJson(i: PlayerInfo) = Json.obj(
    "side" -> i.side.letter.toString,
    "active" -> i.active
  )

  private def playerJson(player: Player) = (player.side.name -> Json.obj("id" -> player.id))

  private def pairingUserJson(playerId: String) = playerId

  private def pairingJson(p: Pairing) = Json.obj(
    "id" -> p.gameId,
    "u" -> Json.arr(p.playerIds.map(oi => pairingUserJson(oi)))
  )
}
