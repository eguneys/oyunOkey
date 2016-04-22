package controllers

import play.api.mvc._, Results._

import oyun.app._
import oyun.api.{ BodyContext }


object User extends OyunController {

  def show(username: String) = OpenBody { implicit ctx =>
    filter(username, none, 1)
  }

  private def filter(
    username: String,
    filterOption: Option[String],
    page: Int,
    status: Results.Status = Results.Ok)(implicit ctx: BodyContext[_]) = ???

}
