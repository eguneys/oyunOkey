package controllers

import play.api.mvc._, Results._

import oyun.app._
import views._

object Page extends OyunController {

  def variantHome = Open { implicit ctx =>
    OptionOk(fuccess(3.some)) {
      case _ => views.html.site.variantHome()
    }
  }

  def variant(key: String) = Open { implicit ctx =>
    (for {
      variant <- okey.variant.Variant.byKey get key
    } yield OptionOk(fuccess(3.some)) {
      case _ => views.html.site.variant(variant)
    }) | notFound
  }
}
