package oyun.app
package templating

import play.twirl.api.Html

object Environment 
    extends oyun.BooleanSteroids
    with scalaz.syntax.std.ToOptionIdOps
    with AssetHelper
    with I18nHelper
    with DateHelper
    with JsonHelper
    with UserHelper
    with GameHelper {
  implicit val OyunHtmlMonoid = scalaz.Monoid.instance[Html](
    (a, b) => Html(a.body + b.body),
    Html(""))
}
