package views.html.game

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import oyun.game.Game

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
  

  def gameIcon(game: Game): Char = game.perfType match {
    case _ if game.hasAi => 'n'
    case Some(p) => p.iconChar
    case _ => '8'
  }
}
