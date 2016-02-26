package controllers

import play.api.libs.json.Json

import oyun.common.{ HTTPRequest }

import oyun.app._
import views._

object Setup extends OyunController {
  private def env = Env.setup

  def hookForm = Open { implicit ctx =>
    if (HTTPRequest isXhr ctx.req) {
      env.forms.hookFilled() map { html.setup.hook(_) }
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
        env.processor.hook(config, uid, HTTPRequest sid req) map hookResponse
      }
    )
  }
}
