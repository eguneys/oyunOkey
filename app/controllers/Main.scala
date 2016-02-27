package controllers

import play.api.mvc._

import oyun.app._

import views._

object Main extends OyunController {
  def notFound(req: RequestHeader): Fu[Result] =
    reqToCtx(req) map { implicit ctx =>
      NotFound(html.base.notFound())
    }
}
