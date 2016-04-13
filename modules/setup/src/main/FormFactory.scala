package oyun.setup

import play.api.data._
import play.api.data.Forms._

import oyun.common.Form._

import oyun.user.UserContext

private[setup] final class FormFactory() {
  import Mappings._
  import FormFactory._

  def masaFilled()(implicit ctx: UserContext): Fu[Form[MasaConfig]] =
    masaConfig map masa(ctx).fill

  def masa(ctx: UserContext) = Form(
    mapping(
      "rounds" -> numberIn(roundChoices),
      "variant" -> number.verifying(validVariantIds contains _),
      "ratingRange" -> optional(ratingRange)
    )(MasaConfig.apply)(MasaConfig.unapply)
  )

  def masaConfig(implicit ctx: UserContext): Fu[MasaConfig] = fuccess(MasaConfig(5, 1, None))

  def hookFilled()(implicit ctx: UserContext): Fu[Form[HookConfig]] =
    hookConfig map hook(ctx).fill

  def hook(ctx: UserContext) = Form(
    mapping(
      "ratingRange" -> optional(ratingRange)
    )(HookConfig.apply)(HookConfig.unapply)
  )

  def hookConfig(implicit ctx: UserContext): Fu[HookConfig] = fuccess(HookConfig(None))
}

object FormFactory {
  import okey.variant._

  val rounds = (5 to 30 by 5)
  val roundChoices = options(rounds, "%d round")

  val validVariants = List(Standard)

  val validVariantIds = validVariants.map(_.id).toSet
}
