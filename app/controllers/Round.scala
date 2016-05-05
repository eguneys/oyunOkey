package controllers

import play.api.mvc._
import play.api.libs.json._

import oyun.api.Context
import oyun.app._
import oyun.game.{ Pov, GameRepo }
import oyun.masa.{ MiniStanding }
import views._

object Round extends OyunController with TheftPrevention {

  private def env = Env.round

  def websocketPlayer(fullId: String) = SocketEither[JsValue] { implicit ctx =>
    GameRepo pov fullId flatMap {
      case Some(pov) =>
        get("sri") match {
          case Some(uid) => requestAiMove(pov) >> env.socketHandler.player(
            pov, uid, ctx.me
          ) map Right.apply
          case None => fuccess(Left(NotFound))
        }
      case None => fuccess(Left(NotFound))
    }
  }

  private def requestAiMove(pov: Pov) = pov.game.playableByAi ?? Env.fishnet.player(pov.game)

  private def renderPlayer(pov: Pov)(implicit ctx: Context): Fu[Result] = {
    negotiate(
      html = myMasa(pov.game.masaId, true) flatMap {
        case (masa) =>
          Env.api.roundApi.player(pov) map { data =>
            Ok(html.round.player(pov, data,
              m = masa))
        }
      },
      api = apiVersion => {
        Env.api.roundApi.player(pov).map { Ok(_) }
      }
    )
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

  private def myMasa(masaId: Option[String], withStanding: Boolean)(implicit ctx: Context): Fu[Option[MiniStanding]] =
    masaId ?? { mid =>
      Env.masa.api.miniStanding(mid, ctx.userId, withStanding)
    }

  def sidesPlayer(gameId: String, side: String) = Open { implicit ctx =>
    OptionFuResult(GameRepo.pov(gameId, side)) { sides(_, true) }
  }

  private def sides(pov: Pov, isPlayer: Boolean)(implicit ctx: Context) =
    myMasa(pov.game.masaId, isPlayer) map {
      case (masa) =>
        Ok(html.game.sides(pov, masa))
    }
}
