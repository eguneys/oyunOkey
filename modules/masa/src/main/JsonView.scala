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
    stand <- standing(masa)
  } yield Json.obj(
    "id" -> masa.id,
    "playerId" -> me,
    "fullName" -> masa.fullName,
    "isStarted" -> masa.isStarted,
    "actives" -> data.actives,
    "pairings" -> data.pairings,
    "standing" -> stand,
    "me" -> myInfo.map(myInfoJson),
    "socketVersion" -> socketVersion
  ).noNull

  def standing(masa: Masa): Fu[JsObject] =
    computeStanding(masa)

  private def computeStanding(masa: Masa): Fu[JsObject] = for {
    rankedPlayers <- PlayerRepo.bestByMasaWithRank(masa.id)
    sheets <- rankedPlayers.map { p =>
      PairingRepo.finishedByPlayerChronological(masa.id, p.player.id) map { pairings =>
        p.player.id -> masa.system.scoringSystem.sheet(masa, p.player.id, pairings)
      }
    }.sequenceFu.map(_.toMap)
  } yield Json.obj(
    "page" -> 1,
    "players" -> rankedPlayers.map(playerJson(sheets, masa))
  )

  private val cachableData = ((id: String) => for {
    pairings <- PairingRepo.recentByMasa(id, 40)
    actives <- PlayerRepo.activePlayers(id)
  } yield CachableData(
    pairings = JsArray(pairings map pairingJson),
    actives = JsObject(actives map activeJson)
  ))

  private def myInfoJson(i: PlayerInfo) = Json.obj(
    "side" -> i.side.letter.toString,
    "active" -> i.active
  )

  private def sheetJson(sheet: ScoreSheet) = sheet match {
    case s: arena.ScoringSystem.Sheet =>
      val o = Json.obj(
        "scores" -> s.scores.reverse.map { score =>
          JsNumber(score.value)
        },
        "total" -> s.total
      )
      o
  }

  private def playerJson(sheets: Map[String, ScoreSheet], masa: Masa)(rankedPlayer: RankedPlayer): JsObject =
    playerJson(sheets get rankedPlayer.player.id, masa, rankedPlayer)

  private def playerJson(sheet: Option[ScoreSheet], masa: Masa, rankedPlayer: RankedPlayer): JsObject = {
    val p = rankedPlayer.player
    //val light = getLightUser(p.userId)
    Json.obj(
      "rank" -> rankedPlayer.rank,
      //"name" -> light.fold(p.userId)(_.name)
      "id" -> p.id,
      "active" -> p.active.option(true),
      "score" -> p.score,
      "sheet" -> sheet.map(sheetJson)
    ).noNull
  }

  private def activeJson(player: Player) = (player.side.name -> Json.obj("id" -> player.id))

  private def pairingUserJson(playerId: String) = JsString(playerId)

  private def pairingJson(p: Pairing) = Json.obj(
    "id" -> p.gameId,
    "u" -> JsArray(p.playerIds.toList map (pairingUserJson)),
    "s" -> 0
  )
}
