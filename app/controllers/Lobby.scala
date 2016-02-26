package controllers

import play.api.libs.json._
import play.api.mvc._

import oyun.app._
import oyun.api.Context

import views._

object Lobby extends OyunController {
  def home = Open { implicit ctx =>
    renderHome(Results.Ok)
  }

  def renderHome(status: Results.Status)(implicit ctx: Context): Fu[Result] = Env.current.preloader() map (html.lobby.home apply _) map { status(_) }

  def socket(apiVersion: Int) = SocketOption[JsValue] { implicit ctx =>
    get("sri") ?? { uid =>
      Env.lobby.socketHandler(uid = uid, user = ctx.me) map some
    }
  }
}
