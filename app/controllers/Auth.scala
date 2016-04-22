package controllers

import play.api.data._, Forms._
import play.api.i18n.Messages.Implicits._
import play.api.mvc._, Results._
import play.api.Play.current

import oyun.api.Context
import oyun.app._
import oyun.common.{ OyunCookie }
import oyun.user.{ UserRepo, User => UserModel }

import views._

object Auth extends OyunController {

  private def env = Env.security
  private def api = env.api
  private def forms = env.forms

  private def authRecovery(implicit ctx: Context): PartialFunction[Throwable, Fu[Result]] = {
    case _ => BadRequest.fuccess
  }

  def login = Open { implicit ctx =>
    val referrer = get("referrer")
    Ok(html.auth.login(api.loginForm, referrer)).fuccess
  }

  def authenticate = OpenBody { implicit ctx =>
    implicit val req = ctx.body
    api.loginForm.bindFromRequest.fold(
      err => negotiate(
        html = Unauthorized(html.auth.login(err, get("referrer"))).fuccess,
        api = _ => Unauthorized(errorsAsJson(err)).fuccess
      ),
      _.fold(InternalServerError("Authentication error").fuccess)(_ => Ok("authsuccess").fuccess)
    )
  }

  def logout = Open { implicit ctx =>
    Ok("lkajdf").fuccess
  }

  def signup = Open { implicit ctx =>
    forms.signup.website match {
      case (form) => Ok(html.auth.signup(form, env.RecaptchaPublicKey)).fuccess
    }
  }

  def signupPost = OpenBody { implicit ctx =>
    implicit val req = ctx.body
    negotiate(
      html = forms.signup.website.bindFromRequest.fold(
        err => BadRequest(html.auth.signup(err, env.RecaptchaPublicKey)).fuccess,
        data => env.recaptcha.verify(data.recaptchaResponse, req).flatMap {
          case false => forms.signup.website match {
            case (form) => BadRequest(html.auth.signup(form fill data, env.RecaptchaPublicKey)).fuccess
          }
          case true =>
            val email = env.emailAddress.validate(data.email) err s"Invalid email ${data.email}"
            UserRepo.create(data.username, data.password, email.some, none)
              .flatten(s"No user could be created for ${data.username}")
              .map(_ -> email).flatMap {
              case (user, email) => env.emailConfirm.send(user, email) >> {
                if (env.emailConfirm.effective) Redirect(routes.Auth.checkYourEmail(user.username)).fuccess
                else saveAuthAndRedirect(user)
              }
            }
        }),
      api = apiVersion => NotFound("asdf").fuccess
    )
  }

  private def saveAuthAndRedirect(user: UserModel)(implicit ctx: Context) = {
    implicit val req = ctx.req
    api.saveAuthentication(user.id) map { sessionId =>
      Redirect(routes.User.show(user.username)) withCookies OyunCookie.session("sessionId", sessionId)
    } recoverWith authRecovery
  }

  def passwordReset = Open { implicit ctx =>
    Ok("kasdjf").fuccess
  }
}
