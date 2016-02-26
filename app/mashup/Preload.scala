package oyun.app
package mashup

import play.api.libs.json._

import oyun.api.Context

final class Preload(
  lobbyApi: oyun.api.LobbyApi) {

  private type Response = JsObject

  def apply()(implicit ctx: Context): Fu[Response] =
    lobbyApi(ctx)
}
