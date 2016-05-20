package oyun.app
package templating

import oyun.game.{ Mode }
import oyun.api.Context

trait SetupHelper { self: I18nHelper =>

  def translatedModeChoices(implicit ctx: Context) = List(
    (Mode.Casual.id.toString, trans.casual.str(), none),
    (Mode.Rated.id.toString, trans.rated.str(), none)
  )

  private def variantTuple(variant: okey.variant.Variant)(implicit ctx: Context): (String, String, Option[String]) =
    (variant.id.toString, variant.name, variant.title.some)

  def translatedVariantChoices(implicit ctx: Context) = List(
    (okey.variant.Standard.id.toString, trans.yuzbirOkey.str(), okey.variant.Standard.title.some)
  )

  def translatedVariantChoicesWithVariants(implicit ctx: Context) =
    translatedVariantChoices(ctx) :+
      variantTuple(okey.variant.DuzOkey)


  def translatedVariantChoicesWithTestVariants(implicit ctx: Context) =
    variantTuple(okey.variant.StandardTest) ::
      variantTuple(okey.variant.DuzOkeyTest) ::
      translatedVariantChoicesWithVariants(ctx)

}
