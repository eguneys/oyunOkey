package controllers

import play.api.mvc._

import oyun.app._

import views._

object Main extends OyunController {
  def notFound(req: RequestHeader): Fu[Result] =
    reqToCtx(req) map { implicit ctx =>
      NotFound(html.base.notFound())
    }

  def lag = Open { implicit ctx =>
    fuccess {
      html.site.lag()
    }
  }

  def mobile = Open { implicit ctx =>
    OptionOk(Prismic getBookmark "mobile-apk") {
      case (doc, resolver) => html.mobile.home(doc, resolver)
    }
  }

}
