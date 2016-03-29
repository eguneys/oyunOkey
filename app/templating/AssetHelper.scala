package oyun.app
package templating

import controllers.routes
import play.twirl.api.Html

trait AssetHelper { self: I18nHelper =>
  def assetVersion = oyun.api.Env.current.assetVersion.get

  val assetDomain = oyun.api.Env.current.Net.AssetDomain

  val assetBaseUrl = s"http://$assetDomain"

  def staticUrl(path: String) = s"$assetBaseUrl${routes.Assets.at(path)}"

  def cssTag(name: String, staticDomain: Boolean = true) = cssAt("stylesheets/" + name, staticDomain)

  def cssAt(path: String, staticDomain: Boolean = true) = Html {
    val href = if (staticDomain) staticUrl(path) else routes.Assets.at(path)
    s"""<link href="$href?v=$assetVersion" type="text/css" rel="stylesheet"/>"""
  }

  def jsTag(name: String) = jsAt("javascripts/" + name)

  def jsTagCompiled(name: String) = jsTag(name)

  def jQueryTag = cdnOrLocal(
    cdn = "//cdnjs.cloudflare.com/ajax/libs/jquery/2.2.0/jquery.min.js",
    test = "window.jQuery",
    local = staticUrl("javascripts/vendor/jquery.min.js"))

  val momentjsTag = cdnOrLocal(
    cdn = "http://cdnjs.cloudflare.com/ajax/libs/moment.js/2.10.6/moment.min.js",
    test = "window.moment",
    local = staticUrl("vendor/moment/min/moment.min.js"))

  def momentLangTag(implicit ctx: oyun.api.Context) = (lang(ctx).language match {
    case "en" => none
    case l => l.some
  }).fold(Html("")) { l =>
    jsAt(s"vendor/moment/locale/$l.js", static = true)
  }

  private def cdnOrLocal(cdn: String, test: String, local: String) = Html {
    s"""<script src="$cdn"></script>"""
    //s"""<script src="$local"></script>"""
  }

  def jsAt(path: String, static: Boolean = true) = Html {
    s"""<script src="${static.fold(staticUrl(path), path)}?v=$assetVersion"></script>"""
  }

  def embedJs(js: String): Html = Html(s"""<script>/* <![CDATA[ */ $js /* ]]> */</script>""")
  def embedJs(js: Html): Html = embedJs(js.body)
}
