package controllers

import play.api.data._, Forms._
import play.api.i18n.Messages.Implicits._
import play.api.libs.json._
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

  private def mobileUserOk(u: UserModel): Fu[Result] =
    funit map { _ =>
      Ok {
        Env.user.jsonView(u)
      }
    }

  private def authenticateUser(u: UserModel)(implicit ctx: Context) = {
    implicit val req = ctx.req

    api.saveAuthentication(u.id) flatMap { sessionId =>
      negotiate(
        html = Redirect {
          get("referrer").filter(_.nonEmpty) orElse req.session.get(api.AccessUri) getOrElse routes.Lobby.home.url
        }.fuccess,
        api = _ => mobileUserOk(u)
      ) map {
        _ withCookies OyunCookie.withSession { session =>
          session + ("sessionId" -> sessionId) - api.AccessUri
        }
      }
    } recoverWith authRecovery
  }

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
      _.fold(InternalServerError("Authentication error").fuccess)(authenticateUser)
    )
  }

  def logout = Open { implicit ctx =>
    implicit val req = ctx.req
    req.session get "sessionId" foreach oyun.security.Store.delete
    negotiate(
      html = fuccess(Redirect(routes.Main.mobile)),
      api = apiVersion => Ok(Json.obj("ok" -> true)).fuccess
    ) map (_ withCookies OyunCookie.newSession)
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
      api = apiVersion => forms.signup.mobile.bindFromRequest.fold(
        err => fuccess(BadRequest(jsonError(errorsAsJson(err)))),
        data => {
          val email = data.email flatMap env.emailAddress.validate
          UserRepo.create(data.username, data.password, email, apiVersion.some)
            .flatten(s"No user could be created for ${data.username}") flatMap authenticateUser
        }
      )
    )
  }

  def checkYourEmail(name: String) = Open { implicit ctx =>
    OptionOk(UserRepo named name) { user =>
      html.auth.checkYourEmail(user)
    }
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
