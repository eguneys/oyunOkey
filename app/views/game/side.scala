package views.html
package game

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import controllers.routes

object side {

  private val separator = " • "

  def apply(pov: oyun.game.Pov,
    m: Option[oyun.masa.Masa])(implicit ctx: Context): Option[Frag] = true option frag(
    meta(pov, m)
  )


  def meta(pov: oyun.game.Pov,
    m: Option[oyun.masa.Masa])(implicit ctx: Context): Option[Frag] = true option {
    import pov._

    div(cls := "game__meta")(
      st.section(
        div(cls := "game__meta__infos", dataIcon := bits.gameIcon(game))(
          div(
            div(cls := "header")(
              div(cls := "setup")(
                frag(
                  (if (game.rated) trans.rated else trans.casual).txt(),
                  separator,
                  bits.variantLink(game.variant, game.variant.name.toUpperCase)
                )
              )
            )
          )
        ),
        div(cls := "game__meta__players")(
          game.players.map { p =>
            div(cls := s"player color-icon is ${p.side.name} text")(
              playerLink(p, withOnline = false)
            )
          }.toList
        ),
        game.finishedOrAborted option {
          st.section(cls := "status")(
            gameEndStatus(game),
            game.winner.map { winner =>
              frag(
                separator
              )
            }
          )
        }
      )
    )
  }
}
