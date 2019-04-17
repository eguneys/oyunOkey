package controllers

import akka.pattern.ask
import play.api.mvc._

import oyun.app._
import oyun.hub.actorApi.captcha.ValidCaptcha
import makeTimeout.large
import views._

object Main extends OyunController {
  def notFound(req: RequestHeader): Fu[Result] =
    reqToCtx(req) map { implicit ctx =>
      NotFound(html.base.notFound())
    }

  def captchaCheck(id: String) = Open { implicit ctx =>
    Env.hub.captcher ? ValidCaptcha(id, ~get("solution")) map {
      case valid: Boolean => Ok(if (valid) 1 else 0)
    }
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

  def websocket = SocketOption { implicit ctx =>
    getSocketUid("sri") ?? { uid =>
      // Env.site.socketHandler(uid, ctx.userId, get("flag")) map some
      Env.site.socketHandler.human(uid, ctx.userId, get("flag")) map some
    }
  }


  def versionedAsset(version: String, file: String) = {
    // println(file)
    // println (Assets.at(path = "/public", file))
    Assets.at(path = "/public", file)
  }
}
