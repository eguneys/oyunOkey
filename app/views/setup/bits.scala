package views.html.setup

import play.api.data.{ Form, Field }

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import controllers.routes

object bits {

  val prefix = "sf_"

  def renderVariant(form: Form[_], variants: List[SelectChoice])(implicit ctx: Context) =
    div(cls := "variant label_select")(
      renderLabel(form("variant"), trans.variant()),
      renderSelect(form("variant"), variants)
    )
  
  def renderSelect(field: Field, options: Seq[SelectChoice]) = select(id := s"$prefix${field.id}", name := field.name, 3)(
    options.map {
      case (value, name, title) => option(
        st.value := value,
        st.title := title,
        field.value.exists(v => v == value) option selected
      )(name)
    }
  )

  def renderRoundMode(form: Form[_])(implicit ctx: Context) =
    div(cls := "round_mode_config")(
      div(cls := "round_choice slider")(
        trans.roundsToPlay(),
        ": ",
        span(form("rounds").value),
        renderInput(form("rounds"))
      )
    )

  def renderRadios(field: Field, options: Seq[SelectChoice]) =
    st.group(cls := "radio")(
      options.map {
        case (key, name, hint) => div(
          input(
            `type` := "radio",
            id := s"$prefix${field.id}_${key}",
            st.name := field.name,
            value := key,
            field.value.has(key) option checked
          ),
          label(
            cls := "required",
            title := hint,
            `for` := s"$prefix${field.id}_$key"
          )(name)
        )
      }
    )

  def renderInput(field: Field) =
    input(name := field.name, value := field.value, `type` := "hidden")

  def renderLabel(field: Field, content: Frag) =
    label(`for` := s"$prefix${field.id}")(content)


  val dataType = attr("data-type")
}
