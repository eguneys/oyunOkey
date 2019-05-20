package views.html.lobby

import play.api.libs.json.{ Json, JsObject }

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._
import oyun.common.String.html.safeJsonValue
import oyun.game.Pov

import controllers.routes

object home {

  def apply(
    data: JsObject,
    masas: List[oyun.masa.Masa],
    playing: List[Pov],
    nbRounds: Int)(implicit ctx: Context) = views.html.base.layout(
    title = "",
      fullTitle = Some(s"oyunkeyf.${if (isProd) "net" else "dev"} • " + trans.freeOnlineOkey.txt()),
      moreJs = frag(
        jsAt(s"compiled/oyunkeyf.lobby${isProd ?? (".min")}.js", defer = true),
        embedJsUnsafe(
          s"""oyunkeyf=oyunkeyf||{};oyunkeyf.lobby=${
safeJsonValue(Json.obj(
"data" -> data,
"i18n" -> i18nJsObject(translations)
))}"""
        )
      ),
      moreCss = cssTag("lobby"),
      okeyground = false,
      openGraph = oyun.app.ui.OpenGraph(
        title = trans.theBestFreeOkeyServer.txt(),
        url = netBaseUrl,
        description = trans.siteDescription.txt()
      ).some,
      deferJs = true) {
    main(cls := List(
      "lobby" -> true))(
      div(cls := "lobby__table")(
        div(cls := "lobby__start")(
          a(href := routes.Setup.hookForm, cls := List(
            "button button-metal config_hook" -> true
          ), trans.createAGame()),
          a(href := routes.Setup.aiForm, cls := List(
            "button button-metal config-ai" -> true
          ), trans.playWithTheMachine())
        ),
        div(cls := "lobby__counters")(
          a(id := "nb_connected_players", href := '#')(trans.nbPlayers(nbPlayersPlaceholder)),
          a(id := "nb_games_in_play", href := '#')(trans.nbGamesInPlay(strong(nbRounds)))
        )
      ),
        div(cls := "lobby_about")(
          a(href := "/about")(trans.aboutX("Oyunkeyf")),
          a(href := "/contact")(trans.contact()),
          a(href := "/mobile")(trans.mobileApp())
        )
    )
  }

  private val translations = List(
  )

  private val nbPlayersPlaceholder = strong("--")

}
