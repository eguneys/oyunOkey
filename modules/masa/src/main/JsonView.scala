package oyun.masa

import play.api.libs.json._
import oyun.common.PimpedJson._

import oyun.common.LightUser

final class JsonView(getLightUser: String => Option[LightUser]) {

  private case class CachableData(
    pairings: JsArray,
    actives: JsObject,
    users: JsObject,
    podium: Option[JsArray])

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
    "greatPlayer" -> GreatPlayer.wikiUrl(masa.name).map { url =>
      Json.obj("name" -> masa.name, "url" -> url)
    },
    "nbPlayers" -> masa.nbPlayers,
    "nbRounds" -> masa.nbRounds,
    "rounds" -> masa.rounds,
    "variant" -> masa.variant.key,
    "isStarted" -> masa.isStarted,
    "isFinished" -> masa.isFinished,
    "actives" -> data.actives,
    "users" -> data.users,
    "pairings" -> data.pairings,
    "standing" -> stand,
    "me" -> myInfo.map(myInfoJson),
    "podium" -> data.podium,
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
    users <- PlayerRepo.allUserPlayers(id)
    podium <- podiumJson(id)
  } yield CachableData(
    pairings = JsArray(pairings map pairingJson),
    actives = JsObject(actives map activeJson),
    users = JsObject(users flatMap playerUserMap),
    podium))

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
    val light = p.userId flatMap getLightUser
    Json.obj(
      "rank" -> rankedPlayer.rank,
      "name" -> light.map(_.name),
      "id" -> p.id,
      "active" -> p.active.option(true),
      "score" -> p.score,
      "sheet" -> sheet.map(sheetJson)
    ).noNull
  }

  private def playerUserMap(player: Player) = player.userId map (player.id -> JsString(_))

  private def activeJson(player: Player) = (player.side.name -> Json.obj("id" -> player.id))

  private def podiumJson(id: String): Fu[Option[JsArray]] =
    MasaRepo finishedById id flatMap {
      _ ?? { masa =>
        PlayerRepo.bestByMasaWithRank(id).flatMap {
          _.map {
            case rp@RankedPlayer(_, player) => for {
              pairings <- PairingRepo.finishedByPlayerChronological(masa.id, player.id)
              sheet = masa.system.scoringSystem.sheet(masa, player.id, pairings)
            } yield playerJson(sheet.some, masa, rp)
          }.sequenceFu
        } map { l => JsArray(l).some }
      }
    }

  private def pairingUserJson(playerId: String) = JsString(playerId)

  private def pairingJson(p: Pairing) = Json.obj(
    "id" -> p.gameId,
    "u" -> JsArray(p.playerIds.toList map (pairingUserJson)),
    "r" -> p.round,
    "s" -> (if (p.finished) p.winner else 0))

}
