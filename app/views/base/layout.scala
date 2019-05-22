package views.html.base

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._
import oyun.common.{ Lang }

import controllers.routes

object layout {

  object bits {
    val doctype = raw("<!doctype html>")
    def htmlTag(implicit lang: Lang) = html(st.lang := lang.language)
    val topComment = raw("""<!- Oyunkeyf is open source! See https://github.com/eguneys/oyun -->""")
    val charset = raw("""<meta charset="utf-8">""")
    val viewport = raw("""<meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover"/>""")
    def pieceSprite(implicit ctx: Context): Frag = pieceSprite()
    def pieceSprite(): Frag =
      link(id := "piece-sprite", href := assetUrl(s"piece-css/standard.css"), tpe := "text/css", rel := "stylesheet")
  }
  import bits._

  private val noTranslate = raw("""<meta name="google" content="notranslate" />""")
  private def fontPreload(implicit ctx: Context) = raw { s"""<link href="https://fonts.googleapis.com/css?family=Noto+Sans|Roboto&display=swap" rel="stylesheet">
"""
  }
  private val manifests = raw("""<link rel="manifest" href="/manifest.json" /><meta name="twitter:site" content="@oyunkeyf" />""")

  private val favicons = raw {
    List(16, 32) map { px =>
      s"""<link rel="icon" type="image/png" href="${staticUrl(s"favicon.$px.png")}" sizes="${px}x${px}"/>"""
    } mkString
  }

  private def dasher(me: oyun.user.User) = raw(s"""<div class="dasher"><a id="user_tag" class="toggle link">${me.username}</a><div id="dasher_app" class="dropdown"></div></div>""")

  private def anonDasher()(implicit ctx: Context) = spaceless(s"""<div class="dasher">
<a class="toggle link anon">
<span title="" data-icon="%"></span>
</a>
<div id="dasher_app" class="dropdown"></div>
</div>
<a href="${routes.Auth.login}?referrer=${ctx.req.path}" class="signin button button-empty">${trans.signIn().render}</a>""")

  private val spaceRegex = """\s{2,}+""".r
  private def spaceless(html: String) = raw(spaceRegex.replaceAllIn(html.replace("\\n", ""), ""))

  private val dataUser = attr("data-user")
  private val dataSocketDomain = attr("data-socket-domain")



  def apply(
    title: String,
    fullTitle: Option[String] = None,
    robots: Boolean = isGloballyCrawlable,
    moreCss: Frag = emptyFrag,
    moreJs: Frag = emptyFrag,
    openGraph: Option[oyun.app.ui.OpenGraph] = None,
    playing: Boolean = false,
    okeyground: Boolean = true,
    deferJs: Boolean = false,
    wrapClass: String = ""
  )(body: Frag)(implicit ctx: Context) = frag(
    doctype,
    htmlTag(ctx.lang)(
      topComment,
      head(
        charset,
        viewport,
        if (isProd) frag(st.headTitle(fullTitle | s"$title  • oyunkeyf.net")
        ) else st.headTitle(s"[dev] ${fullTitle | s"$title  • oyunkeyf.dev"}"),
        cssTag("site"),
        moreCss,
        pieceSprite,
        meta(content := openGraph.fold(trans.siteDescription.txt())(o => o.description), name := "description"),
        //link(id := "favicon", rel:= "shortcut icon", href := staticUrl("images/favicon-32-white.png"), `type` := "image/x-icon"),
        favicons,
        !robots option raw("""<meta content="noindex, nofollow" name="robots">"""),
        noTranslate,
        openGraph.map(_.frags),
        fontPreload
        // manifests
      ),
      st.body(
        dataDev := (!isProd).option("true"),
        dataUser := ctx.userId,
        dataSocketDomain := socketDomain,
        dataAssetUrl := assetBaseUrl,
        dataAssetVersion := assetVersion.value,
        dataTheme := ctx.currentBg
      )(
        siteHeader(),
        div(id := "main-wrap", cls := List(
          wrapClass -> wrapClass.nonEmpty
        ))(body),
        a(id := "reconnecting", cls := "link text", dataIcon := "B")(trans.reconnecting()),
        okeyground option jsTag("vendor/okeyground.min.js"),
        if (isProd)
          jsAt(s"compiled/oyunkeyf.site.min.js", defer = deferJs)
        else frag(
          jsAt("compiled/oyunkeyf.deps.js", defer = deferJs),
          jsAt("compiled/oyunkeyf.site.js", defer = deferJs)),
        moreJs,
        embedJsUnsafe(s"""$timeagoLocaleScript""")
      )
    )
  )

  object siteHeader {

    private val topnavToggle = spaceless("""
<input type="checkbox" id="tn-tg" class="topnav-toggle fullscreen-toggle" aria-label="Navigation">
<label for="tn-tg" class="fullscreen-mask"></label>
<label for="tn-tg" class="hbg"><span class="hbg__in"></span></label>""")

    def apply()(implicit ctx: Context) =
      header(id := "top")(
        div(cls := "site-title-nav")(
          topnavToggle,
          h1(cls := "site-title")(
            a(href := "/")(
              "oyunkeyf",
              span(if (isProd) ".org" else ".dev")
            )
          ),
          topnav()
        ),
        div(cls := "site-buttons")(
          ctx.me map { me =>
            frag(dasher(me))
          } getOrElse {
            anonDasher()
          }
        )
      )
  }
}
