package views.html.setup

import play.api.data.Form
import play.api.mvc.Call

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._
import oyun.setup.{ HookConfig }
import oyun.user.User

import controllers.routes

object forms {

  import bits._

  def hook(form: Form[_])(implicit ctx: Context) = layout(
    form,
    "hook",
    trans.createAGame(),
    routes.Setup.hook("uid-placeholder")
  ) {
  }


  def ai(form: Form[_])(implicit ctx: Context) = layout(form, "ai", trans.playWithTheMachine(), routes.Setup.ai) {
  }

  private def layout(form: Form[_],
    typ: String,
    titleF: Frag,
    route: Call,
    error: Option[Frag] = None)(fields: Frag)(implicit ctx: Context) =
    div(cls := error.isDefined option "error")(
      h2(titleF),
      error.map { e =>
        frag(
          p(cls := "error")(e),
          br,
          a(href := routes.Lobby.home, cls := "button text", dataIcon := "L")(trans.cancel.txt())
        )
      }.getOrElse {
        st.form(action := route, method := "post", novalidate,
          dataType := typ)(
          fields,
            div(cls := "color-submits")(
              button(tpe := "submit",
                cls := s"color-submits__button button button-metal")
            )
        )
      }
    )
  
}
