package views.html

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import controllers.routes

object mobile {

  def apply(apkDoc: io.prismic.Document, resolver: io.prismic.DocumentLinkResolver)(implicit ctx: Context) = base.layout(
    title = "Mobile",
    moreCss = cssTag("mobile")
  ) {
    main(
      div(cls := "mobile page-small box box-pad")(
        h1(trans.playOkeyEverywhere())
      )
    )
  }

}
