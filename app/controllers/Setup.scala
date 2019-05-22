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
      env.forms.masaFilled() map { html.setup.forms.masa(_) }
    } else fuccess {
      Redirect(routes.Lobby.home + "#hook")
    }
  }

  def hook(uid: String) = process(env.forms.masa) { (config, playerRef) => implicit ctx =>
    env.processor.masa(config, playerRef, Uid(uid), HTTPRequest sid ctx.body)
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
