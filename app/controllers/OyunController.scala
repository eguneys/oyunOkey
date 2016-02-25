package controllers

import play.api.mvc._

import oyun.app._
import oyun.api.{ PageData, Context, BodyContext }
import oyun.user.{ UserContext }

private[controllers] trait OyunController
    extends Controller {

  protected def Open(f: Context => Fu[Result]): Action[Unit] =
    Open(BodyParsers.parse.empty)(f)


  protected def Open[A](p: BodyParser[A])(f: Context => Fu[Result]): Action[A] =
    Action.async(p) { req =>
      reqToCtx(req) flatMap { ctx =>
        f(ctx)
      }
    }

  protected def reqToCtx[A](req: Request[A]): Fu[BodyContext[A]] =
  {
    val ctx = UserContext(req)
    pageDataBuilder(ctx) map { Context(ctx, _) }
  }

  private def pageDataBuilder(ctx: UserContext): Fu[PageData] =
    fuccess(PageData anon)
}
