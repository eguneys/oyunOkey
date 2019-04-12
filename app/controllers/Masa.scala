package controllers

import play.api.mvc.{ Result, Cookie }
import play.api.libs.json._

import oyun.api.Context
import oyun.app._
import oyun.common.{ HTTPRequest, OyunCookie }
import oyun.masa.{ System, MasaRepo, PlayerRef, AnonCookie }
import views._

object Masa extends OyunController with TheftPrevention {

  private def env = Env.masa
  private def repo = MasaRepo

  private def masaNotFound(implicit ctx: Context) = NotFound(html.masa.notFound())

  def home(page: Int) = Open { implicit ctx =>
    negotiate(
      html = {
        val playingPaginator = repo.finishedPaginator(maxPerPage = 30, page = page)
        env.api.fetchVisibleMasas zip
          repo.publicCreatedSorted zip
          playingPaginator map({
          case ((visible, created), playing) =>
              Ok(html.masa.home(created, playing, env scheduleJsonView visible))
        }) map NoCache
      },
      api = _ => env.api.fetchVisibleMasas map { masas =>
        Ok(env scheduleJsonView masas)
      }
    )
  }

  def help(sysStr: Option[String]) = Open { implicit ctx =>
    val system = sysStr flatMap {
      case "arena" => System.Arena.some
      case _ => none
    }
    Ok(html.masa.faqPage(system)).fuccess
  }

  def show(id: String) = Open { implicit ctx =>
    playerForReq(id) flatMap { playerOption =>
      val playerId = playerOption map(_.playerId)
      negotiate(
        html = repo byId id flatMap {
          _.fold(masaNotFound.fuccess) { masa =>
            env.version(masa.id).zip(chatOf(masa)).flatMap {
              case (version, chat) =>
                env.jsonView(masa, playerId, version.some) map {
                  html.masa.show(masa, _, chat)
                }
            }
          }
        },
        api = _ => repo byId id flatMap {
          case None => NotFound(jsonError("No such masa")).fuccess
          case Some(masa) => {
            env version(masa.id) flatMap { version =>
              env.jsonView(masa, playerId, version.some) map { Ok(_) }
            }
          }
        }
      )
    }
  }

  def invite(id: String) = Open { implicit ctx: Context =>
    withEnterableMasaOwner(id) { omasa =>
      val side = get("side")
      negotiate(
        html = omasa match {
          case None => masaNotFound.fuccess
          case Some(masa) =>
            env.api.invite(masa.id, side) inject
            Redirect(routes.Masa.show(masa.id))
        },
        api = _ => (omasa match {
          case None =>
            fuccess(jsonError(true))
          case Some(masa) =>
            env.api.invite(masa.id, side) inject
            Json.obj("ok" -> true)
        }) map { Ok(_) }
      )
    }
  }

  private def withEnterableMasaOwner(id: String)(withMasa: Option[oyun.masa.Masa] => Fu[Result])(implicit ctx: Context): Fu[Result] =
    playerForReq(id) zip (repo enterableById id) flatMap {
      case (Some(player), omasa@Some(masa)) if (masa.createdBy == (player.userId | player.playerId)) => withMasa(omasa)
      case _ => withMasa(none)
    }

  def join(id: String) = OpenBody { implicit ctx =>
    val side = get("side")
    playerForReq(id) flatMap { playerOption =>
      val userId = ctx.me map(_.id)
      val ref = playerOption.map(_.ref(ctx.me)) | PlayerRef(user = ctx.me)
      negotiate(
        html = repo enterableById id flatMap {
          case None => masaNotFound.fuccess
          case Some(masa) =>
            env.api.join(masa.id, ref, side = side) inject
            Redirect(routes.Masa.show(masa.id))
        },
        api = _ => OptionFuOk(repo enterableById id) { masa =>
          env.api.join(masa.id, ref, side) inject
            Json.obj("ok" -> true)
        }
      ) flatMap withMasaAnonCookie(ctx.isAnon, ref.playerId)
    }
  }

  def withdraw(id: String) = Open { implicit ctx =>
    OptionFuResult(repo byId id) { masa =>
      OptionFuResult(playerForReq(masa.id)) { player =>
        env.api.withdraw(masa.id, player.playerId) inject {
          if (HTTPRequest.isXhr(ctx.req)) Ok(Json.obj("ok" -> true)) as JSON
          else Redirect(routes.Masa.show(masa.id))
        }
      }
    }
  }

  def form = Open { implicit ctx =>
    Env.setup.forms masaFilled() map(html.masa.form(_))
  }

  def create = OpenBody { implicit ctx =>
    implicit val req = ctx.body
    val userId = ctx.me map (_.id)
    val playerRef = PlayerRef(user = ctx.me)
    Env.setup.forms.masa(ctx).bindFromRequest.fold(
      err => BadRequest(html.masa.form(err)).fuccess,
      setup => {
        env.api.addMasa(setup.masa(), playerRef) map { masa =>
          Redirect(routes.Masa.show(masa.id))
        } flatMap withMasaAnonCookie(ctx.isAnon, playerRef.playerId)
      }
    )
  }

  private def withMasaAnonCookie(cond: Boolean, id: String)(res: Result)(implicit ctx: Context): Fu[Result] =
    cond ?? {
      implicit val req = ctx.req
      fuccess(OyunCookie.cookie(
        AnonCookie.name,
        id,
        maxAge = AnonCookie.maxAge.some,
        httpOnly = false.some) some)
    } map { cookieOption =>
      cookieOption.fold(res) { res.withCookies(_) }
    }

  def websocket(id: String, apiVersion: Int) = SocketOption[JsValue] { implicit ctx =>
    get("sri") ?? { uid =>
      playerForReq(id) flatMap {
        env.socketHandler.join(id, uid, ctx.me, _)
      }
    }
  }

  private def chatOf(masa: oyun.masa.Masa)(implicit ctx: Context) =
    ctx.isAuth ?? {
      Env.chat.api.userChat find masa.id map (_.forUser(ctx.me).some)
    }
}
