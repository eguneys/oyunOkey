package oyun.app
package templating

import oyun.api.Context

trait SetupHelper { self: I18nHelper =>

  def translatedVariantChoices(implicit ctx: Context) = List(
    (okey.variant.Standard.id.toString, trans.yuzbirOkey.str(), okey.variant.Standard.title.some)
  )

  def translatedVariantChoicesWithVariants(implicit ctx: Context) =
    translatedVariantChoices(ctx)

}
