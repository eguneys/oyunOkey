package views.html.site

import controllers.routes
import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

object help {

  def layout(
    title: String,
    active: String,
    contentCls: String = "",
    moreCss: Frag = emptyFrag,
    moreJs: Frag = emptyFrag
  )(body: Frag)(implicit ctx: Context) = views.html.base.layout(
    title = title,
    moreCss = moreCss,
    moreJs = moreJs
  ) {

    val sep = div(cls := "sep")
    def activeCls(c: String) = cls := active.activeO(c)
    main(cls := "page-menu")(
      st.nav(cls := "page-menu__menu subnav")(
       // a(activeCls("about"), href := routes.Page.about)(trans.aboutX("oyunkeyf.net"))
        a(activeCls("lag"), href := routes.Main.lag)("Oyunkeyf'te lag var mı?")
      ),
      div(cls := s"page-menu__content $contentCls")(body)
    )

  }

}
