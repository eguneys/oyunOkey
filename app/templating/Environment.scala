package oyun.app
package templating

import ornicar.scalalib

import oyun.api.Env.{ current => apiEnv }
import oyun.app.ui.ScalatagsTemplate._

object Environment 
    extends scalaz.syntax.std.ToOptionIdOps
    with scalaz.std.OptionFunctions
    with scalaz.std.StringInstances
    with scalalib.Zeros
    with oyun.BooleanSteroids
    with oyun.OptionSteroids
    with oyun.Steroids
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
    with GameHelper {

  type FormWithCaptcha = (play.api.data.Form[_], oyun.common.Captcha)

  // implicit val OyunHtmlMonoid = scalaz.Monoid.instance[Html](n
  //   (a, b) => Html(a.body + b.body),
  //   Html(""))

  def netDomain = apiEnv.Net.Domain
  def netBaseUrl = apiEnv.Net.BaseUrl
  val isGloballyCrawlable = apiEnv.Net.Crawlable

  def isProd = apiEnv.isProd

  val spinner: Frag = raw("""<div class="spinner"><svg viewBox="0 0 40 40"><circle cx=20 cy=20 r=18 fill="none"></circle></svg></div>""")
}
