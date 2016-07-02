package controllers

import play.api.libs.json._
import play.api.mvc._, Results._

import oyun.app._

object Api extends OyunController {

  private val userApi = Env.api.userApi

  def user(name: String) = ApiResult { implicit ctx =>
    userApi one name
  }


  private def ApiResult(js: oyun.api.Context => Fu[Option[JsValue]]) = Open { implicit ctx =>
    js(ctx) map {
      case None => NotFound
      case Some(json) => get("callback") match {
        case None => Ok(json) as JSON
        case Some(callback) => Ok(s"$callback($json)") as JAVASCRIPT
      }
    }
  }
}
