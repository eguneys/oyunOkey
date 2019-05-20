package controllers

import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{ Result, Results }

import oyun.common.{ HTTPRequest }
import oyun.socket.Socket.Uid
import oyun.api.{ Context, BodyContext }
import oyun.common.{ OyunCookie }
import oyun.game.{ GameRepo, AnonCookie }
import oyun.masa.{ Masa => GameMasa, PlayerRef }

import oyun.app._
import views._

object Setup extends OyunController {
  private def env = Env.setup

  def aiForm = Open { implicit ctx =>
    if (HTTPRequest isXhr ctx.req) {
      env.forms.aiFilled map { form =>
        html.setup.forms.ai(form) 
      }
    } else fuccess {
      Redirect(routes.Lobby.home + "#ai")
    }
  }

  def ai = process(env.forms.ai) { (config, playerRef) => implicit ctx =>
    env.processor.ai(config, playerRef)
  }

  def hookForm = Open { implicit ctx =>
    if (HTTPRequest isXhr ctx.req) {
      env.forms.hookFilled() map { html.setup.forms.hook(_) }
    } else fuccess {
      Redirect(routes.Lobby.home + "#hook")
    }
  }

  private def hookResponse(hookId: String) =
    Ok(Json.obj(
      "ok" -> true,
      "hook" -> Json.obj("id" -> hookId))) as JSON

  def hook(uid: String) = OpenBody { implicit ctx =>
    implicit val req = ctx.body

    env.forms.hook(ctx).bindFromRequest.fold(
      err => BadRequest("errorsAsJson(err)".toString).fuccess,
      config => {
        env.processor.hook(config, Uid(uid), HTTPRequest sid req) map hookResponse
      }
    )
  }

  private def process[A](form: Context => Form[A])(op: (A, PlayerRef) => BodyContext[_] => Fu[GameMasa]) =
    OpenBody { implicit ctx =>
      implicit val req = ctx.body
      val playerRef = PlayerRef(user = ctx.me)
      form(ctx).bindFromRequest.fold(
        err => negotiate(
          html = Lobby.renderHome(Results.BadRequest),
          api = _ => jsonFormError(err)
        ),
        config => op(config, playerRef)(ctx) flatMap { masa =>
          fuccess(redirectMasa(masa, playerRef))
        }
      )
    }



  private[controllers] def redirectMasa(masa: GameMasa, playerRef: PlayerRef)(implicit ctx: Context) = {
    val redir = Redirect(routes.Masa.show(masa.id))
    if (ctx.isAuth) redir
    else redir withCookies OyunCookie.cookie(
      AnonCookie.name,
      playerRef.playerId,
      maxAge = AnonCookie.maxAge.some,
      httpOnly = false.some
    )
  }
}
