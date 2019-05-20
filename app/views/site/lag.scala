package views.html.site

import controllers.routes
import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

object lag {

  def apply()(implicit ctx: Context) = help.layout(
    title = "Oyunkeyf'te lag var mı?",
    active = "lag",
    moreCss = cssTag("lag"),
    moreJs = frag(
      jsTag("lag.js")
    )
  ) {
    main(cls := "box box-pad lag")(
      h1(
        "Oyunkeyf'te lag var mı?"
      )
    )
  }
}
