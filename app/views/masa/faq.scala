package views.html
package masa

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import controllers.routes

object faq {

  def page(system: Option[oyun.masa.System])(implicit ctx: Context) = views.html.base.layout(
    title = "Masa SSS",
    moreCss = cssTag("page")
  ) {

    main(cls := "page-small box box-pad page")(
      h1(
        a(href := routes.Masa.home(), dataIcon := "I", cls := "text"),
        system.??(_.toString), "Masa SSS"
      ),
      div(cls := "body")(apply(system = system))
    )

  }

  def apply(system: Option[oyun.masa.System] = None)(implicit ctx: Context) = frag(
    p(trans.arena.willBeNotified()),
    h2(trans.arena.isItRated()),
    p(trans.arena.someRated()),
    h2(trans.arena.howAreScoresCalculated()),
    p(trans.arena.howAreScoresCalculatedAnswer()),
    p(trans.arena.earlyPlayerCantScore()),
    h2(trans.arena.howIsTheWinnerDecided()),
    p(trans.arena.howIsTheWinnerDecidedAnswer()),
    h2(trans.arena.howDoesThePairingWork()),
    p(trans.arena.howDoesThePairingWorkAnswer()),
    h2(trans.arena.howDoesItEnd()),
    p(trans.arena.howDoesItEndAnswer()),
    h2(trans.arena.otherRules()),
    p(trans.arena.otherRulesAnswer())
  )
}
