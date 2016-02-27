package controllers

import play.api.mvc._

import oyun.api.Context
import oyun.app._
import oyun.game.{ Pov, GameRepo }
import views._

object Round extends OyunController {

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
}
