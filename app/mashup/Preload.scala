package oyun.app
package mashup

import play.api.libs.json._

import oyun.masa.{ Masa }
import oyun.game.{ GameRepo, Pov }
import oyun.api.Context

final class Preload(
  countRounds: () => Int,
  lobbyApi: oyun.api.LobbyApi) {

  private type Response = (JsObject, List[Masa], List[Pov], Int)

  def apply(masas: Fu[List[Masa]])(implicit ctx: Context): Fu[Response] =
    lobbyApi(ctx) zip
      masas map {
      case ((data, povs), masas) =>
        (data, masas, povs, countRounds())
    }
}
