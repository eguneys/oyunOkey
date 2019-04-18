package controllers

import play.api.libs.json._
import play.api.mvc._

import oyun.app._
import oyun.api.Context

import views._

object Lobby extends OyunController {
  def home = Open { implicit ctx =>
    negotiate(
      html = renderHome(Results.Ok).map(NoCache),
      api = _ => fuccess {
        Ok(Json.obj(
          "lobby" -> Json.obj(
            "version" -> Env.lobby.history.version)
        ))
      }
    )
  }

  def renderHome(status: Results.Status)(implicit ctx: Context): Fu[Result] = {
    Env.current.preloader(
      masas = Env.masa.cached.promotable.get
    ) map (html.lobby.home.apply _).tupled map { status(_) }
  }

  def socket(apiVersion: Int) = SocketOption[JsValue] { implicit ctx =>
    getSocketUid("sri") ?? { uid =>
      Env.lobby.socketHandler(uid = uid, user = ctx.me) map some
    }
  }
}
