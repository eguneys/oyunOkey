package oyun.app
package templating

import controllers.routes
import play.twirl.api.Html

import oyun.api.Context
import oyun.app.ui.ScalatagsTemplate._
import oyun.common.{ AssetVersion }

trait AssetHelper { self: I18nHelper =>

  def isProd: Boolean


  val assetDomain = oyun.api.Env.current.Net.AssetDomain
  val socketDomain = oyun.api.Env.current.Net.SocketDomain

  val assetBaseUrl = s"//$assetDomain"
  def assetVersion = AssetVersion.current


  def assetUrl(path: String): String = s"$assetBaseUrl/assets/_$assetVersion/$path"

  def staticUrl(path: String) = s"$assetBaseUrl/assets/$path"

  def cssTag(name: String)(implicit ctx: Context): Frag =
    cssTagWithTheme(name, "light")

  def cssTagWithTheme(name: String, theme: String): Frag =
    cssAt(s"css/$name.$theme.${if (isProd) "min" else "dev"}.css")

  private def cssAt(path: String): Frag =
    link(href := assetUrl(path), tpe := "text/css", rel := "stylesheet")


  def jsTag(name: String, defer: Boolean = false): Frag =
    jsAt("javascripts/" + name, defer = defer)

  def jsAt(path: String, defer: Boolean = false): Frag = script(
    defer option deferAttr,
    src := assetUrl(path))

  val jQueryTag = raw {
    s"""<script src="${staticUrl("javascripts/vendor/jquery.min.js")}"></script>"""
  }

  // val momentjsTag = cdnOrLocal(
  //   cdn = "//cdnjs.cloudflare.com/ajax/libs/moment.js/2.10.6/moment.min.js",
  //   test = "window.moment",
  //   local = staticUrl("vendor/moment/min/moment.min.js"))

  // def momentLangTag(implicit ctx: oyun.api.Context) = (ctxLang(ctx).language match {
  //   case "en" => none
  //   case l => l.some
  // }).fold(Html("")) { l =>
  //   jsAt(s"vendor/moment/locale/$l.js", static = true)
  // }

  def embedJsUnsafe(js: String)(implicit ctx: Context): Frag = raw {
    s"""<script>$js</script>"""
  }

}
