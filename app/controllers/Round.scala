package controllers

import play.api.mvc._
import play.api.libs.json._

import oyun.api.Context
import oyun.app._
import oyun.game.{ Pov, GameRepo, Game => GameModel, PlayerRef }
import oyun.masa.{ MiniStanding }
import views._

object Round extends OyunController with TheftPrevention {

  private def env = Env.round

  def websocketWatcher(gameId: String, side: String) = SocketOption[JsValue] { implicit ctx =>
    get("sri") ?? { uid =>
      env.socketHandler.watcher(
        gameId = gameId,
        sideName = side,
        uid = uid,
        user = ctx.me)
    }
  }

  def websocketPlayer(fullId: String) = SocketEither[JsValue] { implicit ctx =>
    proxyPov(fullId) flatMap {
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
    OptionFuResult(proxyPov(fullId)) { pov =>
      renderPlayer(pov)
    }
  }

  def watcher(gameId: String, side: String) = Open { implicit ctx =>
    proxyPov(gameId, side) flatMap {
      case Some(pov) =>
        watch(pov)
      case None => notFound
    }
  }

  private def proxyPov(gameId: String, side: String): Fu[Option[Pov]] = okey.Side(side) ?? { c =>
    env.roundProxyGame(gameId) map2 { (g: GameModel) => g pov c }
  }

  private def proxyPov(fullId: String): Fu[Option[Pov]] = {
    val ref = PlayerRef(fullId)
    env.roundProxyGame(ref.gameId) map {
      _ flatMap { _ playerIdPov ref.playerId }
    }
  }

  def watch(pov: Pov)(implicit ctx: Context): Fu[Result] =
    playablePovForReq(pov.game) match {
      case Some(player) => renderPlayer(pov withSide player.side)
      case _ => negotiate(
        html = {
          // why withStanding false?
          myMasa(pov.game.masaId, true) zip
            Env.api.roundApi.watcher(pov) map {
              case (masa, data) =>
                Ok(html.round.watcher(pov, data, masa))
            }
        },
        api = apiVersion => Env.api.roundApi.watcher(pov) map { Ok(_) }
      )
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
