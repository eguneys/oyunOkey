package oyun.setup

import play.api.data._
import play.api.data.Forms._

import oyun.user.UserContext

private[setup] final class FormFactory() {
  import Mappings._


  def masaFilled()(implicit ctx: UserContext): Fu[Form[MasaConfig]] =
    masaConfig map masa(ctx).fill

  def masa(ctx: UserContext) = Form(
    mapping(
      "ratingRange" -> optional(ratingRange)
    )(MasaConfig.apply)(MasaConfig.unapply)
  )

  def masaConfig(implicit ctx: UserContext): Fu[MasaConfig] = fuccess(MasaConfig(None))

  def hookFilled()(implicit ctx: UserContext): Fu[Form[HookConfig]] =
    hookConfig map hook(ctx).fill

  def hook(ctx: UserContext) = Form(
    mapping(
      "ratingRange" -> optional(ratingRange)
    )(HookConfig.apply)(HookConfig.unapply)
  )

  def hookConfig(implicit ctx: UserContext): Fu[HookConfig] = fuccess(HookConfig(None))
}
