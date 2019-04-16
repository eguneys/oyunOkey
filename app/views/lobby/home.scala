package views.html.lobby

import play.api.libs.json.{ JsObject }

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._
import oyun.game.Pov
import oyun.masa.Masa

import controllers.routes

object home {

  def apply(
    data: JsObject,
    masas: List[Masa],
    playing: List[Pov],
    nbRounds: Int)(implicit ctx: Context) = views.html.base.layout(
    title = "",
      fullTitle = Some("oyunkeyf.net • " + trans.freeOnlineOkey.txt()),
      baseline = Some(frag(
        a(id := "nb_connected_players", href :=(routes.User.list.toString))(trans.nbPlayers.frag(strong("?"))),
        a(id := "nb_games_in_play", href := "#")(trans.nbGamesInPlay.frag(strong(nbRounds)))
        )),
      moreJs = frag(
        jsAt(s"compiled/oyunkeyf.lobby.js"),
        embedJs {
          s"""oyunkeyf = oyunkeyf || {}; oyunkeyf.lobby = { data: ${J.stringify(data) } }; """
        }
      ),
      moreCss = cssTag("home.css"),
      openGraph = oyun.app.ui.OpenGraph(
        title = trans.theBestFreeOkeyServer.txt(),
        url = netBaseUrl,
        description = trans.freeOnlineOkeyGamePlayOkeyNowInACleanInterfaceNoRegistrationNoAdsNoPluginRequiredPlayOkeyWithComputerFriendsOrRandomOpponents.txt()
      ).some) {

    frag(
      div(cls := List(
        "lobby_and_ground" -> true
      ))(
        div(id := "hooks_wrap"),
        frag(
          div(cls := "undertable")(
            div(cls := "undertable_top")(
              a(cls := "more hint--bottom", dataHint := trans.seeAllMasas.txt(), href := routes.Masa.home())(trans.more.frag(), " »"),
              span(cls := "title text", dataIcon := "g")(trans.openMasas())
            ),
            div(id := "enterable_masas", cls := "enterable_list undertable_inner scroll-shadow-hard")(views.html.masa.enterable(masas))
          ),
          div(id := "start_buttons", cls := "oyun_ground")(
            a(cls := "fat button config_masa", href := routes.Masa.form, onclick := "return false")(trans.createAGame())
          )
        )
      ),
      if(playing.headOption.isDefined) {
        div(cls := "undertable")(
          div(cls := "undertable_top")(
            span(cls := "title text", dataIcon := "g")(trans.nbGamesInPlay(playing.size))
          ),
          div(cls := "nowplaying_list undertable_inner")(views.html.game.enterable(playing))
        )
      }
    )
  }

}
