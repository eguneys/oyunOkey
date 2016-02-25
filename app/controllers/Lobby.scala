package controllers

import play.api.mvc._

import oyun.app._
import oyun.api.Context

import views._

object Lobby extends OyunController {
  def home = Open { implicit ctx =>
    renderHome(Results.Ok)
  }

  def renderHome(status: Results.Status)(implicit ctx: Context): Fu[Result] = fuccess(status(html.lobby.home()))
}
