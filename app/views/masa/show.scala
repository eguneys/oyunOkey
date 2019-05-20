package views.html
package masa

import play.api.libs.json.Json

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._
import oyun.common.String.html.safeJsonValue
import oyun.masa.Masa

import controllers.routes

object show {

def apply(
  m: Masa,
  data: play.api.libs.json.JsObject,
  chat: Option[oyun.chat.UserChat])(implicit ctx: Context) = views.html.base.layout(
  title = s"${m.fullName} #${m.id}",
    moreJs =  frag(
      jsAt(s"compiled/oyunkeyf.masa${isProd ?? (".min")}.js"),
      embedJsUnsafe(s"""oyunkeyf = oyunkeyf || {};oyunkeyf.masa=${
safeJsonValue(Json.obj(
          "data" -> data,
          "i18n" -> bits.jsI18n(),
          "userId" -> ctx.userId
))
}""")
    ),
    moreCss = cssTag("masa.show"),
    okeyground = false,
    openGraph = oyun.app.ui.OpenGraph(
      title = s"${m.fullName}: ${m.variant.name} #${m.id}",
      url = s"$netBaseUrl${routes.Masa.show(m.id).url}",
      description = s"${m.nbPlayers} oyuncu ${m.fullName} masasında oynuyor." +
        m.winnerId.fold("Kazanan henüz belirlenmedi.") { winnerId =>
          s"${usernameOrId(winnerId)} kazandı!"
        }
    ).some
)(frag(
  main(cls := s"masa")(
    st.aside(cls := "masa__side")(masa.side(m)),
    div(cls := "masa_main")(div(cls := "box")),
    m.isCreated option div(cls := "masa__faq")(
      faq(m.system.some)
    )
  )
))
}
