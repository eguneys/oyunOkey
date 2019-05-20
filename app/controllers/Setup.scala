package controllers

import play.api.data.Form
import play.api.libs.json.Json

import oyun.common.{ HTTPRequest }
import oyun.socket.Socket.Uid
import oyun.api.{ Context, BodyContext }
import oyun.common.{ OyunCookie }
import oyun.game.{ GameRepo, Pov, AnonCookie }

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

  def ai = process(env.forms.ai) { config => implicit ctx =>
    env.processor ai config
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

  private def process[A](form: Context => Form[A])(op: A => BodyContext[_] => Fu[Pov]) =
    OpenBody { implicit ctx =>
      implicit val req = ctx.body
      form(ctx).bindFromRequest.fold(
        err => negotiate(
          html = Lobby.renderHome(Results.BadRequest),
          api = _ => jsonFormError(err)
        ),
        config => op(config)(ctx) flatMap { pov =>
          negotiate(
            html = fuccess(redirectPov(pov)),
            api = apiVersion => Env.api.roundApi.player(pov) map { data =>
              Created(data) as JSON
            }
          )
        }
      )
    }

  private[controllers] def redirectPov(pov: Pov)(implicit ctx: Context) = {
    val redir = Redirect(routes.Round.watcher(pov.gameId, "east"))
    if (ctx.isAuth) redir
    else redir withCookies OyunCookie.cookie(
      AnonCookie.name,
      pov.playerId,
      maxAge = AnonCookie.maxAge.some,
      httpOnly = false.some
    )
  }
}
