package controllers

import oyun.app._

import play.api.mvc._, Results._
import play.api.data._, Forms._

object Cli extends OyunController {

  private lazy val form = Form(tuple(
    "command" -> nonEmptyText,
    "password" -> nonEmptyText
  ))
  
  def command = OpenBody { implicit ctx =>
    implicit val req = ctx.body
    form.bindFromRequest.fold(
      err => fuccess(BadRequest("invalid cli call")), {
        case (command, password) => CliAuth(password) {
          Env.api.cli(command.split(" ").toList) map { res => Ok(res) }
        }
      })
  }

  private def CliAuth(password: String)(op: => Fu[Result]): Fu[Result] =
    oyun.user.UserRepo.checkPasswordById(Env.api.CliUsername, password) flatMap {
      _.fold(op, fuccess(Unauthorized))
    }

}
