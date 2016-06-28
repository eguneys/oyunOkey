package controllers

import play.api.libs.json._
import play.api.mvc._, Results._

import oyun.app._
import oyun.app.mashup.GameFilterMenu
import oyun.api.{ BodyContext }

import oyun.user.{ User => UserModel, UserRepo }
import views._


object User extends OyunController {

  private def env = Env.user

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
      leaderboards <- env.cached.leaderboards
      nbAllTime <- env.cached topNbGame nb
      nbDay <- fuccess(Nil)
      // masaWinners <- Env.masa.winners scheduled nb
      online <- env.cached top50Online true
      res <- negotiate(
        html = fuccess(Ok(html.user.list(
          //masaWinners = masaWinners,
          online = online,
          leaderboards = leaderboards,
          nbDay = nbDay,
          nbAllTime = nbAllTime))),
        api = _ =>  fuccess {
          implicit val lpWrites = OWrites[UserModel.LightPerf](env.jsonView.lightPerfIsOnline)
          Ok(Json.obj(
            "yuzbir" -> leaderboards.yuzbir))
        })
    } yield res
  }


  def perfStat(username: String, perfKey: String) = Open { implicit ctx =>
    OptionFuResult(UserRepo named username) { u =>
      if ((u.disabled || (u.lame && !ctx.is(u))) && true) notFound
      else oyun.rating.PerfType(perfKey).fold(notFound) { perfType =>
        for {
          // perfStat <- Env.perfStat.get(u, perfType)
          //data <- Env.perfStat.jsonView(u)
          data <- fuccess(play.api.libs.json.Json.obj())
          response <- negotiate(
            html = Ok(html.user.perfStat(u, perfType, data)).fuccess,
            api = _ => Ok(data).fuccess)
        } yield response
      }
    }
  }
}
