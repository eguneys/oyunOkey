package oyun.masa

import play.api.libs.json._
import oyun.common.PimpedJson._
import scala.concurrent.duration._

import oyun.common.LightUser
import oyun.game.{ Game, GameRepo }

final class JsonView(getLightUser: String => Option[LightUser]) {

  private case class CachableData(
    pairings: JsArray,
    actives: JsObject,
    users: JsObject,
    players: JsObject,
    featured: Option[JsObject],
    podium: Option[JsArray])

  def apply(masa: Masa,
    me: Option[String],
    socketVersion: Option[Int]): Fu[JsObject] = for {
    data <- cachableData(masa.id)
    myInfo <- me ?? { PlayerRepo.playerInfo(masa.id, _) }
    stand <- standing(masa)
  } yield Json.obj(
    "id" -> masa.id,
    "createdBy" -> masa.createdBy,
    "playerId" -> me,
    "fullName" -> masa.fullName,
    "greatPlayer" -> GreatPlayer.wikiUrl(masa.name).map { url =>
      Json.obj("name" -> masa.name, "url" -> url)
    },
    "nbPlayers" -> masa.nbPlayers,
    "nbRounds" -> masa.nbRounds,
    "rounds" -> masa.rounds,
    "scores" -> masa.scores,
    "variant" -> masa.variant.key,
    "isStarted" -> masa.isStarted,
    "isFinished" -> masa.isFinished,
    "actives" -> data.actives,
    "users" -> data.users,
    "players" -> data.players,
    "pairings" -> data.pairings,
    "standing" -> stand,
    "me" -> myInfo.map(myInfoJson),
    "featured" -> data.featured,
    "podium" -> data.podium,
    "socketVersion" -> socketVersion
  ).noNull

  def standing(masa: Masa): Fu[JsObject] =
    computeStanding(masa)

  private def fetchFeaturedGame(masa: Masa): Fu[Option[FeaturedGame]] =
    masa.featuredId.ifTrue(masa.isStarted) ?? PairingRepo.byId flatMap {
      _ ?? { pairing =>
          GameRepo game pairing.gameId flatMap {
            // TODO ?? make it work
            _ match { case Some(game) =>
              fuccess(Some(FeaturedGame(game)))
              case _ => fuccess(None)
            }
          }
      }
    }

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

  private val cachableData = oyun.memo.AsyncCache[String, CachableData](id =>
    for {
      pairings <- PairingRepo.recentByMasa(id, 40)
      masa <- MasaRepo byId id
      actives <- PlayerRepo.activePlayers(id)
      users <- PlayerRepo.allUserPlayers(id)
      players <- PlayerRepo.allByMasa(id)
      featured <- masa ?? fetchFeaturedGame
      podium <- podiumJson(id)
    } yield CachableData(
      pairings = JsArray(pairings map pairingJson),
      actives = JsObject(actives map activeJson),
      users = JsObject(users flatMap playerUserMap),
      players = JsObject(players map (p => p.id -> playerInfoJson(p))),
      featured = featured map featuredJson,
      podium),
    timeToLive = 1 second)

  private def featuredJson(featured: FeaturedGame) = {
    val game = featured.game
    Json.obj(
      "id" -> game.id,
      "fen" -> (okey.format.Forsyth >>| (game.toOkey, okey.Side.EastSide))
    )
  }

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

  private def playerInfoJson(p: Player): JsObject = {
    val light = p.userId flatMap getLightUser
    Json.obj(
      "userId" -> p.userId,
      "ai" -> p.aiLevel,
      "name" -> light.map(_.name)
    )
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
      "rating" -> p.rating,
      "ratingDiff" -> p.ratingDiff,
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
    "s" -> p.status.id,
    "w" -> (if (p.finished) p.winner else 0))

}
