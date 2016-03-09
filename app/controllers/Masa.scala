package controllers

import play.api.libs.json._

import oyun.api.Context
import oyun.app._
import oyun.masa.{ MasaRepo }
import views._

object Masa extends OyunController {

  private def env = Env.masa
  private def repo = MasaRepo

  private def masaNotFound(implicit ctx: Context) = NotFound(html.masa.notFound())

  def show(id: String) = Open { implicit ctx =>
    repo byId id flatMap {
      _.fold(masaNotFound.fuccess) { masa =>
        env.version(masa.id) flatMap { version =>
          env.jsonView(masa, ctx.userId, version.some) map {
            html.masa.show(masa, _)
          }
        }
      }
    }
  }

  def form = Open { implicit ctx =>
    Env.setup.forms masaFilled() map(html.masa.form(_))
  }

  def create = OpenBody { implicit ctx =>
    implicit val req = ctx.body
    Env.setup.forms.masa(ctx).bindFromRequest.fold(
      err => BadRequest(html.masa.form(err)).fuccess,
      setup => env.api.createMasa(setup.masa()) map { masa =>
        Redirect(routes.Masa.show(masa.id))
      }
    )
  }

  def websocket(id: String, apiVersion: Int) = SocketOption[JsValue] { implicit ctx =>
    get("sri") ?? { uid =>
      env.socketHandler.join(id, uid, ctx.me)
    }
  }
}
