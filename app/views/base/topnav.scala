package views.html.base

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import controllers.routes

object topnav {

  private def linkTitle(url: String, name: Frag)(implicit ctx: Context) =
    a(href := url)(name)

  def apply()(implicit ctx: Context) = st.nav(id := "topnav", cls := "hover")(
    st.section(
      linkTitle("/", frag(
        span(cls := "play")(trans.play()),
        span(cls := "home")("oyunkeyf.net")
      )),
      div(role := "group")(
        a(href := "/?any#hook")(trans.createAGame()),
        frag(
          a(href := routes.Masa.home())(trans.masas())
        )
      )
    )
    // st.section(
    //   linkTitle(routes.User.list.toString, trans.community()),
    //   div(role := "group")(
    //     a(href := routes.User.list)(trans.players()),
    //     a(href := routes.ForumCateg.index)(trans.forum())
    //   )
    // )
  )

}
