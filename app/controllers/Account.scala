package controllers

import play.api.mvc._, Results._


import oyun.api.Context
import oyun.app._
import oyun.common.OyunCookie
import oyun.user.{ UserRepo, User => UserModel }
import views._

object Account extends OyunController {


  private def env = Env.user


  def info = Open { implicit ctx =>
    negotiate(
      html = notFound,
      api = _ => ctx.me match {
        case None => fuccess(unauthorizedApiResult)
        case Some(me) =>
          Env.pref.api.getPref(me) zip
          oyun.game.GameRepo.urgentGames(me) map {
            case (prefs, povs) =>
              Env.current.bus.publish(oyun.user.User.Active(me), 'userActive)
              Ok {
                import play.api.libs.json._
                import oyun.pref.JsonView._
                Env.user.jsonView(me) ++ Json.obj(
                  "prefs" -> prefs,
                  "nowPlaying" -> JsArray(povs take 20 map Env.api.lobbyApi.nowPlaying))
              }
          }
      }
    )
  }

}
