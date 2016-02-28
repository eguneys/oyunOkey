package controllers

import play.api.libs.iteratee.{ Iteratee, Enumerator }
import play.api.libs.json.{ Json, JsValue }
import play.api.mvc._
import play.api.mvc.WebSocket.FrameFormatter
import play.twirl.api.Html

import oyun.app._
import oyun.api.{ PageData, Context, HeaderContext, BodyContext }
import oyun.user.{ UserContext }

private[controllers] trait OyunController
    extends Controller
    with RequestGetter {

  protected implicit def OyunHtmlToResult(content: Html): Result = Ok(content)

  protected implicit final class OyunPimpedResult(result: Result) {
    def fuccess = scala.concurrent.Future successful result
  }

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

  protected def OptionFuResult[A](fua: Fu[Option[A]])(op: A => Fu[Result])(implicit ctx: Context) =
    fua flatMap { _.fold(notFound(ctx))(a => op(a)) }

  def notFound(implicit ctx: Context): Fu[Result] = {
    Main notFound ctx.req
  }

  protected def reqToCtx(req: RequestHeader): Fu[HeaderContext] =
  {
    val ctx = UserContext(req, None)
    pageDataBuilder(ctx) map { Context(ctx, _) }
  }

  protected def reqToCtx[A](req: Request[A]): Fu[BodyContext[A]] =
  {
    val ctx = UserContext(req, None)
    pageDataBuilder(ctx) map { Context(ctx, _) }
  }

  private def pageDataBuilder(ctx: UserContext): Fu[PageData] =
    fuccess(PageData anon)

  // protected def errorsAsJson(form: play.api.data.Form[_])(implicit lang: play.api.i18n.Messages) =
  //   oyun.common.Form errorsAsJson form
}
