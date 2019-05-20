package controllers

import play.api.libs.iteratee.{ Iteratee, Enumerator }
import play.api.libs.json.{ Json, JsValue, JsObject, JsString, JsArray, Writes }
import play.api.data.Form
import play.api.mvc._
import play.api.http._
import play.api.mvc.WebSocket.FrameFormatter
import scalatags.Text.Frag

import oyun.app._
import oyun.common.{ OyunCookie, HTTPRequest, Lang }
import oyun.api.{ PageData, Context, HeaderContext, BodyContext }
import oyun.security.{ FingerprintedUser }
import oyun.user.{ UserContext, User => UserModel }

private[controllers] trait OyunController
    extends Controller
    with ContentTypes
    with RequestGetter 
    with ResponseWriter {

  protected implicit def OyunFragToResult(content: Frag): Result = Ok(content)

  protected implicit final class OyunPimpedResult(result: Result) {
    def fuccess = scala.concurrent.Future successful result
  }

  implicit def ctxLang(implicit ctx: Context) = ctx.lang
  implicit def ctxReq(implicit ctx: Context) = ctx.req

  protected def NoCache(res: Result): Result = res.withHeaders(
    CACHE_CONTROL -> "no-cache, no-store, must-revalidate", EXPIRES -> "0"
  )

  protected def SocketEither[A: FrameFormatter](f: Context => Fu[Either[Result, (Iteratee[A, _], Enumerator[A])]]) =
    WebSocket.tryAccept[A] { req => reqToCtx(req) flatMap f }

  protected def SocketOption[A: FrameFormatter](f: Context => Fu[Option[(Iteratee[A, _], Enumerator[A])]]) =
    WebSocket.tryAccept[A] { req =>
      reqToCtx(req) flatMap f map {
        case None => Left(NotFound(Json.obj("error" -> "socket resource not found")))
        case Some(pair) => Right(pair)
      }
    }

  protected def Open(f: Context => Fu[Result]): Action[Unit] =
    Open(BodyParsers.parse.empty)(f)


  protected def Open[A](p: BodyParser[A])(f: Context => Fu[Result]): Action[A] =
    Action.async(p) { req =>
      reqToCtx(req) flatMap { ctx =>
        f(ctx)
      }
    }

  protected def OpenBody(f: BodyContext[_] => Fu[Result]): Action[AnyContent] =
    OpenBody(BodyParsers.parse.anyContent)(f)

  protected def OpenBody[A](p: BodyParser[A])(f: BodyContext[_] => Fu[Result]): Action[A] = Action.async(p)(req => reqToCtx(req) flatMap f)

  protected def Auth(f: Context => UserModel => Fu[Result]): Action[Unit] =
    Auth(BodyParsers.parse.empty)(f)

  protected def Auth[A](p: BodyParser[A])(f: Context => UserModel => Fu[Result]): Action[A] =
    Action.async(p) { req =>
      reqToCtx(req) flatMap { implicit ctx =>
        // ctx.me.fold(authenticationFailed) { me =>
        //   Env.i18n.requestHandler.forUser(req, ctx.me).fold(f(ctx)(me))(fuccess)
        // }
        ctx.me.fold(authenticationFailed(ctx))(f(ctx))
      }
    }

  protected def OptionResult[A](fua: Fu[Option[A]])(op: A => Result)(implicit ctx: Context) =
    OptionFuResult(fua) { a => fuccess(op(a)) }

  protected def OptionFuResult[A](fua: Fu[Option[A]])(op: A => Fu[Result])(implicit ctx: Context) =
    fua flatMap { _.fold(notFound(ctx))(a => op(a)) }

  protected def OptionOk[A, B: Writeable: ContentTypeOf](fua: Fu[Option[A]])(op: A => B)(implicit ctx: Context): Fu[Result] =
    OptionFuOk(fua) { a => fuccess(op(a)) }

  protected def OptionFuOk[A, B: Writeable: ContentTypeOf](fua: Fu[Option[A]])(op: A => Fu[B])(implicit ctx: Context) =
    fua flatMap { _.fold(notFound(ctx))(a => op(a) map { Ok(_) }) }

  def notFound(implicit ctx: Context): Fu[Result] = {
    Main notFound ctx.req
  }

  def jsonError[A: Writes](err: A): JsObject = Json.obj("error" -> err)

  protected def authenticationFailed(implicit ctx: Context): Fu[Result] =
    negotiate(
      html = fuccess {
        implicit val req = ctx.req
        Redirect(routes.Auth.signup)
      },
      api = _ => unauthorizedApiResult.fuccess
    )

  protected def unauthorizedApiResult = Unauthorized(jsonError("Login required"))

  protected def negotiate(html: => Fu[Result], api: Int => Fu[Result])(implicit ctx: Context): Fu[Result] =
    (oyun.api.Mobile.Api.requestVersion(ctx.req) match {
      case Some(1) => api(1) map (_ as JSON)
      case _ => html
    }) map (_.withHeaders("Vary" -> "Accept"))

  protected def reqToCtx(req: RequestHeader): Fu[HeaderContext] =
  {
    restoreUser(req) flatMap { d =>
      val ctx = UserContext(req, d.map(_.user), oyun.i18n.I18nLangPicker(req, d.map(_.user)))
      pageDataBuilder(ctx) map { Context(ctx, _) }
    }
  }

  protected def reqToCtx[A](req: Request[A]): Fu[BodyContext[A]] =
  {
    restoreUser(req) flatMap { d =>
      val ctx = UserContext(req, d.map(_.user), oyun.i18n.I18nLangPicker(req, d.map(_.user)))
      pageDataBuilder(ctx) map { Context(ctx, _) }
    }
  }

  private def pageDataBuilder(ctx: UserContext): Fu[PageData] =
    fuccess(PageData anon)

  private def restoreUser(req: RequestHeader): Fu[Option[FingerprintedUser]] =
    Env.security.api restoreUser req addEffect {
      _ ifTrue (HTTPRequest isSynchronousHttp req) foreach { d =>
        Env.current.bus.publish(oyun.user.User.Active(d.user), 'userActive)
      }
    }

  protected def Reasonable(page: Int, max: Int = 40, errorPage: => Fu[Result] = BadRequest("resource too old").fuccess)(result: => Fu[Result]): Fu[Result] =
    if (page < max) result else errorPage

  protected def errorsAsJson(form: play.api.data.Form[_])(implicit lang: Lang) = {
    val json = JsObject(
      form.errors.groupBy(_.key).mapValues { errors =>
        JsArray {
          errors.map { e =>
            JsString(e.message)
          }
        }
      }
    )
    json validate jsonGlobalErrorRenamer getOrElse json
  }

  protected def jsonFormError(err: Form[_])(implicit lang: Lang) =
    fuccess(BadRequest(errorsAsJson(err)))

  private val jsonGlobalErrorRenamer = {
    import play.api.libs.json._
    __.json update (
      (__ \ "global").json copyFrom (__ \ "").json.pick
    ) andThen (__ \ "").json.prune
  }
}
