package views.html.masa

import play.api.libs.json.Json

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._
import oyun.common.String.html.safeJsonValue
import oyun.masa.Masa

import controllers.routes

object home {

  def apply(
    scheduled: List[Masa],
    playing: oyun.common.paginator.Paginator[Masa],
    json: play.api.libs.json.JsObject)(implicit ctx: Context) =
    views.html.base.layout(
      title = trans.masas.txt(),
      moreCss = cssTag("masa.home"),
      wrapClass = "full-screen-force",
      moreJs = frag(
        jsAt(s"compiled/oyunkeyf.masaSchedule${isProd ?? (".min")}.js"),
        embedJsUnsafe(s"""var app=OyunkeyfMasaSchedule.app(document.querySelector('.masa-chart'), ${
safeJsonValue(Json.obj("data" -> json,
"i18n" -> bits.jsI18n()
))
});
var d=oyunkeyf.StrongSocket.defaults;d.params.flag="masa";d.events.reload=app.update;""")
      ),
      openGraph = oyun.app.ui.OpenGraph(
        url = s"$netBaseUrl${routes.Masa.home().url}",
        title = trans.masaHomeTitle.txt(),
        description = trans.masaHomeDescription.txt()
      ).some
    ) {
      main(cls := "masa-home")(
        st.aside(cls := "masa-home__side")(
          h2(
            trans.leaderboard()
          ),
          ul(cls := "leaderboard")(

          ),
          p(cls := "masa__links")(
            br,
            a(href := routes.Masa.help("arena".some))(trans.masaFAQ())
          ),
          h2(trans.oyunkeyfMasas()),
          div(cls := "scheduled")(
            scheduled.map { m =>
              a(href := routes.Masa.show(m.id), dataIcon := masaIconChar(m))(
                strong(m.name),
                momentFromNow(m.createdAt)
              )
            }
          )
        ),
        st.section(cls := "masa-home__schedule box")(
          div(cls := "box__top")(
            h1(trans.masas())
          ),
          div(cls := "masa-chart")
        ),
        div(cls := "masa-home__list box")(
        )
      )
    }
}
