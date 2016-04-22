package controllers

import play.api.mvc._, Results._

import oyun.app._
import views._

object Page extends OyunController {

  private def bookmark(name: String) = Open { implicit ctx =>
    OptionOk(Prismic getBookmark name) {
      case (doc, resolver) => views.html.site.page(doc, resolver)
    }
  }

  def tos = bookmark("tos")

  def variantHome = Open { implicit ctx =>
    OptionOk(fuccess(3.some)) {
      case _ => views.html.site.variantHome()
    }
  }

  def variant(key: String) = Open { implicit ctx =>
    (for {
      variant <- okey.variant.Variant.byKey get key
    } yield OptionOk(Prismic getVariant variant) {
      case (doc, resolver) => views.html.site.variant(doc, resolver, variant)
    }) | notFound
  }
}
