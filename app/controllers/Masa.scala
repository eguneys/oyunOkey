package controllers

import play.api.mvc.{ Result, Cookie }
import play.api.libs.json._

import oyun.api.Context
import oyun.app._
import oyun.common.{ OyunCookie }
import oyun.masa.{ MasaRepo, PlayerRef, AnonCookie }
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
    val playerRef = PlayerRef()
    Env.setup.forms.masa(ctx).bindFromRequest.fold(
      err => BadRequest(html.masa.form(err)).fuccess,
      setup => {
        env.api.createMasa(setup.masa(), playerRef) map { masa =>
          Redirect(routes.Masa.show(masa.id))
        } flatMap withMasaAnonCookie(ctx.isAnon, playerRef.id)
      }
    )
  }

  private def withMasaAnonCookie(cond: Boolean, id: String)(res: Result)(implicit ctx: Context): Fu[Result] =
    cond ?? {
      implicit val req = ctx.req
      fuccess(OyunCookie.cookie(
        AnonCookie.name,
        id,
        maxAge = AnonCookie.maxAge.some,
        httpOnly = false.some) some)
    } map { cookieOption =>
      cookieOption.fold(res) { res.withCookies(_) } ~ { println }
    }

  def websocket(id: String, apiVersion: Int) = SocketOption[JsValue] { implicit ctx =>
    get("sri") ?? { uid =>
      env.socketHandler.join(id, uid, ctx.me)
    }
  }
}
