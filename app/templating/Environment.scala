package oyun.app
package templating

import ornicar.scalalib

import play.twirl.api.Html

import oyun.api.Env.{ current => apiEnv }

object Environment 
    extends scalaz.syntax.std.ToOptionIdOps
    with scalaz.std.OptionFunctions
    with scalaz.std.StringInstances
    with scalalib.Zeros
    with oyun.BooleanSteroids
    with oyun.OptionSteroids
    with HtmlHelper
    with AssetHelper
    with RequestHelper
    with I18nHelper
    with NotificationHelper
    with DateHelper
    with NumberHelper
    with StringHelper
    with JsonHelper
    with SetupHelper
    with FormHelper
    with AiHelper
    with UserHelper
    with ForumHelper
    with PaginatorHelper
    with MasaHelper
    with GameHelper
    with ui.ScalatagsTwirl {

  type FormWithCaptcha = (play.api.data.Form[_], oyun.common.Captcha)

  implicit val OyunHtmlMonoid = scalaz.Monoid.instance[Html](
    (a, b) => Html(a.body + b.body),
    Html(""))

  def netDomain = apiEnv.Net.Domain
  def netBaseUrl = apiEnv.Net.BaseUrl

  def isProd = apiEnv.isProd
}
