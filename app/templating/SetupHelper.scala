package oyun.app
package templating

import oyun.api.Context

trait SetupHelper { self: I18nHelper =>

  private def variantTuple(variant: okey.variant.Variant)(implicit ctx: Context): (String, String, Option[String]) =
    (variant.id.toString, variant.name, variant.title.some)

  def translatedVariantChoices(implicit ctx: Context) = List(
    (okey.variant.Standard.id.toString, trans.yuzbirOkey.str(), okey.variant.Standard.title.some)
  )

  def translatedVariantChoicesWithVariants(implicit ctx: Context) =
    translatedVariantChoices(ctx)


  def translatedVariantChoicesWithTestVariants(implicit ctx: Context) =
    variantTuple(okey.variant.StandardTest) ::
      translatedVariantChoices(ctx)

}
