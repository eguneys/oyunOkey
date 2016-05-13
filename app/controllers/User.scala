package controllers

import play.api.libs.json._
import play.api.mvc._, Results._

import oyun.app._
import oyun.app.mashup.GameFilterMenu
import oyun.api.{ BodyContext }

import oyun.user.{ User => UserModel, UserRepo }
import views._


object User extends OyunController {

  def show(username: String) = OpenBody { implicit ctx =>
    filter(username, none, 1)
  }

  def showFilter(username: String, filterName: String, page: Int) = OpenBody { implicit ctx =>
    filter(username, filterName.some, page)
  }

  private def filter(
    username: String,
    filterOption: Option[String],
    page: Int,
    status: Results.Status = Results.Ok)(implicit ctx: BodyContext[_]) = 
    OptionFuResult(UserRepo named username) { u =>
      if (u.enabled) negotiate(
        html = {
          userShow(u, filterOption, page)
        }.map { status(_) },//.mon(_.http.response.user.show.website),
        api = _ => NotFound("lakdjf").fuccess)
        // api = _ => userGames(u, filterOption, page).map {
        //   case (filterName, pag) => Ok(Env.api.userGameApi.filter(filterName, pag))
        // }.mon(_.http.response.user.show.mobile))
      else negotiate(
        html = fufail("lakdjf"), // fuccess(NotFound(html.user.disabled(u))),
        api = _ => fuccess(NotFound(jsonError("No such user, or account closed"))))
    }

  private def userShow(u: UserModel, filterOption: Option[String], page: Int)(implicit ctx: BodyContext[_]) = for {
    info <- Env.current.userInfo(u, ctx)
    filters = GameFilterMenu(info, ctx.me, filterOption)
  } yield html.user.show(u, info, filters)


  private def userGames(u: UserModel, filterOption: Option[String], page: Int)(implicit ctx: BodyContext[_]) = {
    Ok("user games").fuccess
  }

  def list = Open { implicit ctx =>
    val nb = 10
    for {
      res <- negotiate(
        html = fuccess(Ok(html.user.list())),
        api = _ =>  fuccess {
          Ok(Json.obj())
        }
      )
    } yield res
  }
}
