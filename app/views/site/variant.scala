package views
package html.site

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import controllers.routes

object variant {

  def show(
    doc: io.prismic.Document,
    resolver: io.prismic.DocumentLinkResolver,
    variant: okey.variant.Variant)(implicit ctx: Context) = layout(
    title = s"${variant.name} • ${variant.title}",
      klass = "box-pad page variant")(
    h1(cls := "text")(variant.name),
        h2(cls := "headline")(variant.title),
        div(cls := "body")(raw(~doc.getHtml("variant.content", resolver)))
  )

  def home()(implicit ctx: Context) = layout(
    title = "Oyunkeyf varyantlar",
    klass = "variants")(
    h1("Oyunkeyf varyantlar"),
      div(cls := "body box__pad")
  )

  private def layout(title: String,
    klass: String)(body: Modifier*)(implicit ctx: Context) = views.html.base.layout(
    title = title,
      moreCss = cssTag("variant"))(
    main(cls := "page-menu")(
      div(cls := s"page-menu__content box $klass")(body)
    )
  )
}
