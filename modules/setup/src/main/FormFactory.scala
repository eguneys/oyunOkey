package oyun.setup

import play.api.data._
import play.api.data.Forms._

import oyun.user.UserContext

private[setup] final class FormFactory() {
  import Mappings._

  def hookFilled()(implicit ctx: UserContext): Fu[Form[HookConfig]] =
    hookConfig map hook(ctx).fill

  def hook(ctx: UserContext) = Form(
    mapping(
      "ratingRange" -> optional(ratingRange)
    )(HookConfig.apply)(HookConfig.unapply)
  )

  def hookConfig(implicit ctx: UserContext): Fu[HookConfig] = fuccess(HookConfig(None))
}
