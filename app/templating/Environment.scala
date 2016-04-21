package oyun.app
package templating

import ornicar.scalalib

import play.twirl.api.Html

import oyun.api.Env.{ current => apiEnv }

object Environment 
    extends scalaz.syntax.std.ToOptionIdOps
    with scalaz.std.StringInstances
    with scalalib.Zero.Instances
    with oyun.BooleanSteroids
    with oyun.OptionSteroids
    with AssetHelper
    with I18nHelper
    with DateHelper
    with JsonHelper
    with SetupHelper
    with UserHelper
    with MasaHelper
    with GameHelper {
  implicit val OyunHtmlMonoid = scalaz.Monoid.instance[Html](
    (a, b) => Html(a.body + b.body),
    Html(""))

  def isProd = apiEnv.isProd
}
