package views.html
package round

import play.api.libs.json.Json

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._
import oyun.common.String.html.safeJsonValue
import oyun.game.Pov

import controllers.routes

object player {

  def apply(
    pov: Pov,
    data: play.api.libs.json.JsObject,
    m: Option[oyun.masa.MasaMiniView])(implicit ctx: Context) = {

    bits.layout(
      variant = pov.game.variant,
      title = s"${trans.play.txt()}",
      moreJs = frag(
        roundTag,
        embedJsUnsafe(s"""oyunkeyf=window.oyunkeyf||{};onload=function() {
OyunkeyfRound.boot(${
safeJsonValue(Json.obj(
"data" -> data,
"i18n" -> jsI18n(pov.game),
"userId" -> ctx.userId
))
})}
""")
      ),
      openGraph = povOpenGraph(pov).some,
      okeyground = false,
      playing = true
    )(
      main(cls := "round")(
        st.aside(cls := "round__side")(
          bits.side(pov, data, m)
        ),
        bits.roundAppPreload(pov, true),
        div(cls := "round__underboard")(
          // bits.crosstable(cross, pov.game)
        ),
        div(cls := "round__underchat")(bits underchat pov.game)
      )
    )
  }

}
