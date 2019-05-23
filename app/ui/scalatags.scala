package oyun.app
package ui

import ornicar.scalalib.Zero

import scalatags.text.Builder
import scalatags.Text.{ Aggregate, Cap }
import scalatags.Text.all._

trait ScalatagsAttrs {
  lazy val minlength = attr("minlength")
  lazy val dataAssetUrl = attr("data-asset-url")
  lazy val dataAssetVersion = attr("data-asset-version")
  lazy val dataDev = attr("data-dev")
  lazy val dataTheme = attr("data-theme")

  lazy val dataIcon = attr("data-icon")
  lazy val dataHint = attr("data-hint")
  lazy val dataHref = attr("data-href")
  lazy val novalidate = attr("novalidate").empty
  val datetimeAttr = attr("datetime")
  lazy val deferAttr = attr("defer").empty
}

trait ScalatagsSnippets extends Cap {
  import scalatags.Text.all._

  val nbsp = raw("&nbsp;")
  val timeTag = tag("time")
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
    with ScalatagsSnippets
    with ScalatagsPrefix {

  val trans = oyun.i18n.I18nKeys
  def main = scalatags.Text.tags2.main

  implicit val playCallAttr = genericAttr[play.api.mvc.Call]
}

object ScalatagsTemplate extends ScalatagsTemplate

trait ScalatagsExtensions {

  implicit val stringValueAttr = new AttrValue[StringValue] {
    def apply(t: scalatags.text.Builder, a: Attr, v: StringValue): Unit =
      t.setAttr(a.name, scalatags.text.Builder.GenericAttrValueSource(v.value))
  }

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
