package oyun.app
package templating

import oyun.game.{ Mode }
import oyun.api.Context
import oyun.i18n.{ I18nKeys => trans }

trait SetupHelper { self: I18nHelper =>

  type SelectChoice = (String, String, Option[String])

  def translatedModeChoices(implicit ctx: Context) = List(
    (Mode.Casual.id.toString, trans.casual.txt(), none),
    (Mode.Rated.id.toString, trans.rated.txt(), none)
  )

  private def variantTuple(variant: okey.variant.Variant)(implicit ctx: Context): (String, String, Option[String]) =
    (variant.id.toString, variant.name, variant.title.some)

  def translatedVariantChoices(implicit ctx: Context) = List(
    (okey.variant.Standard.id.toString, trans.yuzbirOkey.txt(), okey.variant.Standard.title.some)
  )

  def translatedVariantChoicesWithVariants(implicit ctx: Context) =
    translatedVariantChoices(ctx) :+
      variantTuple(okey.variant.DuzOkey)


  def translatedVariantChoicesWithTestVariants(implicit ctx: Context) =
    variantTuple(okey.variant.StandardTest) ::
      variantTuple(okey.variant.DuzOkeyTest) ::
      translatedVariantChoicesWithVariants(ctx)

}
