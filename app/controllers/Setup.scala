package controllers

// import oyun.common.{ HTTPRequest }

import oyun.app._
import views._

object Setup extends OyunController {
  private def env = Env.setup

  def hookForm = Open { implicit ctx =>
    // val result = if (HTTPRequest isXhr ctx.req) {
    //   env.forms.hookFilled() map { html.setup.hook(_) }
    // } else {
    //   Redirect(routes.Lobby.home + "#hook")
    // }

    env.forms.hookFilled() map { html.setup.hook(_) }
  }

  def hook(uid: String) = OpenBody { implicit ctx =>
    implicit val req = ctx.body

    env.forms.hook(ctx).bindFromRequest.fold(
      err => BadRequest("errorsAsJson(err)".toString).fuccess,
      config => BadRequest("errorsAsJson(err)".toString).fuccess
    )
  }
}
