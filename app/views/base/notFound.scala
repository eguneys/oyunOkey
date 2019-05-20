package views.html
package base


import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import controllers.routes

object notFound {

  def apply()(implicit ctx: Context) = layout(
    title = "Sayfa bulunamadı",
    moreCss = cssTag("not-found")
  )(
    main(cls := "not-found page-small box box-pad")(
      header(
        h1("404"),
        div(
          strong("Sayfa bulunamadı!"),
          p(
            "Geri dön ",
            a(href := routes.Lobby.home)("ana sayfaya")
          )
        )
      )
    )
  )

}
