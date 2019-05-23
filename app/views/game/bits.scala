package views.html.game

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import controllers.routes

object bits {

  def variantLink(
    variant: okey.variant.Variant,
    name: String) = a(
    cls := "variant-link",
      href := routes.Page.variant(variant.key).url,
      rel := "nofollow",
      target := "_blank",
      title := variant.title
  )(name)
  
}
