package controllers

import play.api.libs.iteratee.{ Iteratee, Enumerator }
import play.api.libs.json.{ Json, JsValue, JsObject, JsArray, Writes }
import play.api.mvc._
import play.api.http._
import play.api.mvc.WebSocket.FrameFormatter
import play.twirl.api.Html

import oyun.app._
import oyun.api.{ PageData, Context, HeaderContext, BodyContext }
import oyun.security.{ FingerprintedUser }
import oyun.user.{ UserContext }

private[controllers] trait OyunController
    extends Controller
    with ContentTypes
    with RequestGetter {

  protected implicit def OyunHtmlToResult(content: Html): Result = Ok(content)

  protected implicit final class OyunPimpedResult(result: Result) {
    def fuccess = scala.concurrent.Future successful result
  }

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

  protected def negotiate(html: => Fu[Result], api: Int => Fu[Result])(implicit ctx: Context): Fu[Result] =
    (oyun.api.Mobile.Api.requestVersion(ctx.req) match {
      case Some(1) => api(1) map (_ as JSON)
      case _ => html
    }) map (_.withHeaders("Vary" -> "Accept"))

  protected def reqToCtx(req: RequestHeader): Fu[HeaderContext] =
  {
    val ctx = UserContext(req, None)
    pageDataBuilder(ctx) map { Context(ctx, _) }
  }

  protected def reqToCtx[A](req: Request[A]): Fu[BodyContext[A]] =
  {
    restoreUser(req) flatMap { d =>
      val ctx = UserContext(req, d.map(_.user))
      pageDataBuilder(ctx) map { Context(ctx, _) }
    }
  }

  private def pageDataBuilder(ctx: UserContext): Fu[PageData] =
    fuccess(PageData anon)

  private def restoreUser(req: RequestHeader): Fu[Option[FingerprintedUser]] =
    Env.security.api restoreUser req

  protected def errorsAsJson(form: play.api.data.Form[_])(implicit lang: play.api.i18n.Messages) =
    oyun.common.Form errorsAsJson form
}
