package oyun.app
package templating

import play.twirl.api.Html

object Environment 
    extends oyun.BooleanSteroids
    with AssetHelper
    with I18nHelper
    with JsonHelper {
  implicit val OyunHtmlMonoid = scalaz.Monoid.instance[Html](
    (a, b) => Html(a.body + b.body),
    Html(""))
}
