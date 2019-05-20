package oyun.app
package ui

import ornicar.scalalib.Zero

import play.twirl.api.Html

import scalatags.text.Builder
import scalatags.Text.{ Aggregate, Cap }
import scalatags.Text.all._

trait ScalatagsAttrs {
  lazy val minlength = attr("minlength")
  lazy val dataAssetUrl = attr("data-asset-url")
  lazy val dataAssetVersion = attr("data-asset-version")
  lazy val dataDev = attr("data-dev")

  lazy val dataIcon = attr("data-icon")
  lazy val dataHint = attr("data-hint")
  lazy val dataHref = attr("data-href")
  lazy val novalidate = attr("novalidate").empty
  lazy val deferAttr = attr("defer").empty
}

trait ScalatagsBundle extends Cap
    with Attrs
    with scalatags.text.Tags
    with Aggregate

trait ScalatagsPrefix {
  object st extends Cap with Attrs with scalatags.text.Tags {
    val group = tag("group")
    val headTitle = tag("title")
    val nav = tag("nav")
    val section = tag("section")
    val article = tag("article")
    val aside = tag("aside")

    val frameborder = attr("frameborder")
  }

}

trait ScalatagsTemplate extends Styles
    with ScalatagsBundle
    with ScalatagsAttrs
    with ScalatagsExtensions
    with ScalatagsPrefix {

  val trans = oyun.i18n.I18nKeys
  def main = scalatags.Text.tags2.main

  implicit val playCallAttr = genericAttr[play.api.mvc.Call]
}

object ScalatagsTemplate extends ScalatagsTemplate

trait ScalatagsTwirl extends ScalatagsPlay

// what to import in twirl templates containing scalatags forms
// Allows `*.rows := 5`
trait ScalatagsTwirlForm extends ScalatagsPlay with Cap with Aggregate {
  object * extends Cap with Attrs with ScalatagsAttrs
}

object ScalatagsTwirlForm extends ScalatagsTwirlForm

trait ScalatagsPlay {

  implicit def fragToPlayHtml(frag: Frag): Html = Html(frag.render)

  implicit def playHtmlToFrag(html: Html): Frag = RawFrag(html.body)

  @inline implicit def fragToHtml(frag: Frag) = new FragToHtml(frag)
  
}

final class FragToHtml(private val self: Frag) extends AnyVal {

  def toHtml: Html = Html(self.render)
  
}

trait ScalatagsExtensions {

  implicit val charAttr = genericAttr[Char]

  implicit val optionStringAttr = new AttrValue[Option[String]] {
    def apply(t: scalatags.text.Builder, a: Attr, v: Option[String]): Unit = {
      v foreach { s =>
        t.setAttr(a.name, scalatags.text.Builder.GenericAttrValueSource(s))
      }
    }
  }

  implicit val classesAttr = new AttrValue[List[(String, Boolean)]] {
    def apply(t: scalatags.text.Builder, a: Attr, m: List[(String, Boolean)]): Unit = {
      val cls = m collect { case (s, true) => s } mkString " "
      if (cls.nonEmpty) t.setAttr(a.name, scalatags.text.Builder.GenericAttrValueSource(cls))
    }
  }

  val emptyFrag: Frag = new StringFrag("")
  implicit val OyunFragZero: Zero[Frag] = Zero.instance(emptyFrag)

}
