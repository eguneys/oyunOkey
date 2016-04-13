package controllers

import play.api.mvc.{ Result, Cookie }
import play.api.libs.json._

import oyun.api.Context
import oyun.app._
import oyun.common.{ HTTPRequest, OyunCookie }
import oyun.masa.{ MasaRepo, PlayerRef, AnonCookie }
import views._

object Masa extends OyunController with TheftPrevention {

  private def env = Env.masa
  private def repo = MasaRepo

  private def masaNotFound(implicit ctx: Context) = NotFound(html.masa.notFound())

  def show(id: String) = Open { implicit ctx =>
    playerForReq(id) flatMap { playerOption =>
      val playerId = playerOption map(_.id)
      negotiate(
        html = repo byId id flatMap {
          _.fold(masaNotFound.fuccess) { masa =>
            env.version(masa.id) flatMap { version =>
              env.jsonView(masa, playerId, version.some) map {
                html.masa.show(masa, _)
              }
            }
          }
        },
        api = _ => repo byId id flatMap {
          case None => NotFound(jsonError("No such masa")).fuccess
          case Some(masa) => {
            env version(masa.id) flatMap { version =>
              env.jsonView(masa, playerId, version.some) map { Ok(_) }
            }
          }
        }
      )
    }
  }


  def join(id: String) = OpenBody { implicit ctx =>
    val side = get("side")
    playerForReq(id) flatMap { playerOption =>
      val ref = playerOption.map(_.ref) | PlayerRef()
      negotiate(
        html = repo enterableById id map {
          case None => masaNotFound
          case Some(masa) =>
            env.api.join(masa.id, ref, side = side)
            Redirect(routes.Masa.show(masa.id))
        },
        api = _ => OptionFuOk(repo enterableById id) { masa =>
          env.api.join(masa.id, ref, side)
          fuccess(Json.obj("ok" -> true))
        }
      ) flatMap withMasaAnonCookie(ctx.isAnon, ref.id)
    }
  }

  def withdraw(id: String) = Open { implicit ctx =>
    OptionFuResult(repo byId id) { masa =>
      OptionResult(playerForReq(masa.id)) { player =>
        env.api.withdraw(masa.id, player.id)
        if (HTTPRequest.isXhr(ctx.req)) Ok(Json.obj("ok" -> true)) as JSON
        else Redirect(routes.Masa.show(masa.id))
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
      cookieOption.fold(res) { res.withCookies(_) }
    }

  def websocket(id: String, apiVersion: Int) = SocketOption[JsValue] { implicit ctx =>
    get("sri") ?? { uid =>
      env.socketHandler.join(id, uid, ctx.me)
    }
  }
}
