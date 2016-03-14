package controllers

import play.api.mvc._
import play.api.libs.json._

import oyun.api.Context
import oyun.app._
import oyun.game.{ Pov, GameRepo }
import views._

object Round extends OyunController with TheftPrevention {

  private def env = Env.round

  def websocketPlayer(fullId: String) = SocketEither[JsValue] { implicit ctx =>
    GameRepo pov fullId flatMap {
      case Some(pov) =>
        get("sri") match {
          case Some(uid) => env.socketHandler.player(
            pov, uid, ctx.me
          ) map Right.apply
          case None => fuccess(Left(NotFound))
        }
      case None => fuccess(Left(NotFound))
    }
  }

  private def renderPlayer(pov: Pov)(implicit ctx: Context): Fu[Result] = {
    Env.api.roundApi.player(pov) map { data =>
      Ok(html.round.player(pov, data))
    }
  }

  def player(fullId: String) = Open { implicit ctx =>
    OptionFuResult(GameRepo pov fullId) { pov =>
      renderPlayer(pov)
    }
  }

  def watcher(gameId: String, side: String) = Open { implicit ctx =>
    GameRepo.pov(gameId, side) flatMap {
      case Some(pov) =>
        watch(pov)
      case None => notFound
    }
  }

  def watch(pov: Pov)(implicit ctx: Context): Fu[Result] =
    playablePovForReq(pov.game) match {
      case Some(player) => renderPlayer(pov withSide player.side)
      case _ => Ok("watcher").fuccess
    }
}
