package oyun.app
package templating

import play.api.data._
import play.twirl.api.Html
import oyun.api.Context

import oyun.i18n.{ I18nDb, I18nKeys }

trait FormHelper { self: I18nHelper =>

  private val errNames = Map(
    "error.minLength" -> I18nKeys.textIsTooShort,
    "error.maxLength" -> I18nKeys.textIsTooLong,
    "captcha.fail" -> I18nKeys.invalidCaptcha)

  def errMsg(form: Field)(implicit ctx: Context): Html = errMsg(form.errors)
  def errMsg(form: Form[_])(implicit ctx: Context): Html = errMsg(form.errors)

  def errMsg(errors: Seq[FormError])(implicit ctx: Context): Html = Html {
    errors map { e =>
      val msg = transKey(e.message, I18nDb.Site, e.args) match {
        case m if m == e.message => errNames.get(e.message).fold(e.message)(_.txt())
        case m => m
      }
      s"""<p class="error">$msg</p>"""
    } mkString
  }

  object form3 extends ui.ScalatagsPlay {

    import ui.ScalatagsTemplate._

    private val idPrefix = "form3"

    def id(field: Field): String = s"$idPrefix-${field.id}"

    private def groupLabel(field: Field) = label(cls := "form-label", `for` := id(field))
    private val helper = small(cls := "form-help")


    private def errors(errs: Seq[FormError])(implicit ctx: Context): Frag = errs map error
    private def errors(field: Field)(implicit ctx: Context): Frag = errors(field.errors)
    private def error(err: FormError)(implicit ctx: Context): Frag =
      p(cls := "error")(transKey(err.message, I18nDb.Site, err.args))

    private def validationModifiers(field: Field): Seq[Modifier] = field.constraints collect {
      case ("constraint.minLength", Seq(m: Int)) => minlength := m
      case ("constraint.maxLength", Seq(m: Int)) => maxlength := m
      case ("constraint.min", Seq(m: Int)) => min := m
      case ("constraint.max", Seq(m: Int)) => max := m
    }

    def group(
      field: Field,
      labelContent: Frag,
      klass: String = "",
      half: Boolean = false,
      help: Option[Frag] = None
    )(content: Field => Frag)(implicit ctx: Context): Html =
      div(cls := List(
        "form-group" -> true,
        "is-invalid" -> field.hasErrors,
        "form-half" -> half,
        klass -> klass.nonEmpty))(
        groupLabel(field)(labelContent),
          content(field),
          errors(field),
          help map { helper(_) }
      )

    def input(field: Field, typ: String = "", klass: String = ""): BaseTagType =
      st.input(
        st.id := id(field),
        name := field.name,
        value := field.value,
        `type` := typ.nonEmpty.option(typ),
        cls := List("form-control" -> true, klass -> klass.nonEmpty)
      )(validationModifiers(field))

    def textarea(
      field: Field,
      klass: String = "")(modifiers: Modifier*): Html =
      st.textarea(
        st.id := id(field),
        name := field.name,
        cls := List("form-control" -> true, klass -> klass.nonEmpty)
      )(validationModifiers(field))(modifiers)(~field.value)

    val actions = div(cls := "form-actions")
    def actionsHtml(html: Frag): Html = actions(html)

    def submit(
      content: Frag,
      icon: Option[String] = Some("E"),
      nameValue: Option[(String, String)] = None,
      klass: String = "",
      confirm: Option[String] = None
    ): Html = button(
      `type` := "submit",
      dataIcon := icon,
      name := nameValue.map(_._1),
      value := nameValue.map(_._2),
      cls := List(
        "submit button" -> true,
        "text" -> icon.isDefined,
        "confirm" -> confirm.nonEmpty,
        klass -> klass.nonEmpty
      ),
      title := confirm
    )(content)

    def hidden(field: Field, value: Option[String] = None): Html =
      st.input(
        st.id := id(field),
        name := field.name,
        st.value := value.orElse(field.value),
        `type` := "hidden"
      )

  }

}
