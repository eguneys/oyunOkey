package oyun.app
package templating

import ornicar.scalalib

import play.twirl.api.Html

import oyun.api.Env.{ current => apiEnv }

object Environment 
    extends scalaz.syntax.std.ToOptionIdOps
    with scalaz.std.OptionFunctions
    with scalaz.std.StringInstances
    with scalalib.Zero.Instances
    with oyun.BooleanSteroids
    with oyun.OptionSteroids
    with AssetHelper
    with RequestHelper
    with I18nHelper
    with DateHelper
    with StringHelper
    with JsonHelper
    with SetupHelper
    with FormHelper
    with UserHelper
    with MasaHelper
    with GameHelper {
  implicit val OyunHtmlMonoid = scalaz.Monoid.instance[Html](
    (a, b) => Html(a.body + b.body),
    Html(""))

  def netDomain = apiEnv.Net.Domain
  def netBaseUrl = apiEnv.Net.BaseUrl

  def isProd = apiEnv.isProd
}
