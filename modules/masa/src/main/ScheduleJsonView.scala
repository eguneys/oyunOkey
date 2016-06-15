package oyun.masa

import play.api.libs.json._

import oyun.common.LightUser
import oyun.common.PimpedJson._
import oyun.rating.PerfType
import oyun.user.User

final class ScheduleJsonView(
  getLightUser: String => Option[LightUser]) {

  def apply(masas: VisibleMasas) = Json.obj(
    "created" -> masas.created.map(masaJson),
    "started" -> masas.started.map(masaJson),
    "finished" -> masas.finished.map(masaJson))


  private def masaJson(masa: Masa) = Json.obj(
    "id" -> masa.id,
    "createdBy" -> masa.createdBy,
    "system" -> masa.system.toString.toLowerCase,
    "rated" -> masa.mode.rated,
    "mOnly" -> masa.membersOnly,
    "fullName" -> masa.fullName,
    "nbPlayers" -> masa.nbPlayers,
    "nbRounds" -> masa.nbRounds,
    "rounds" -> masa.rounds,
    "scores" -> masa.scores,
    "variant" -> Json.obj(
      "key" -> masa.variant.key,
      "short" -> masa.variant.shortName,
      "name" -> masa.variant.name),
    "createdAt" -> masa.createdAt,
    "status" -> masa.status.id,
    "winner" -> masa.winnerId.flatMap(getLightUser).map(userJson),
    "perf" -> masa.perfType.map(perfJson)    
  )

  private def userJson(u: LightUser) = Json.obj(
    "id" -> u.id,
    "name" -> u.name
  )

  private def perfJson(p: PerfType) = Json.obj(
    "icon" -> p.iconChar.toString,
    "name" -> p.name
  )
}
