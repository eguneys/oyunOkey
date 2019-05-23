package views.html.masa

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import controllers.routes

object bits {
import oyun.app.ui.ScalatagsTemplate._

  def notFound()(implicit ctx: Context) =
    views.html.base.layout(
      title = trans.masaNotFound.txt()
    ) {
      main(cls := "page-small box box-pad")(
        h1(trans.masaNotFound()),
        p(trans.masaDoesNotExist()),
        p(trans.masaMayHaveBeenCancelled()),
        br,
        br,
        a(href := routes.Masa.home())(trans.returnToMasasHomepage())
      )
    }

  def enterable(masas: List[oyun.masa.Masa]) =
    table(cls := "masas")(
      masas map { m =>
        tr(
          td(cls := "name")(
            a(cls := "text", dataIcon := masaIconChar(m), href := routes.Masa.show(m.id))(m.name)
          ),
          td(),
          td(m.roundString),
          td(dataIcon := "r", cls := "text")(m.nbPlayers)
        )
      }
    )

  def jsI18n()(implicit ctx: Context) = i18nJsObject(translations)

  private val translations = List(
    trans.join,
    trans.invite,
    trans.withdraw,
    trans.youArePlaying,
    trans.joinTheGame,
    trans.waitingPlayers,
    trans.aiBot,
    trans.emptySeat,
    trans.roundX,
    trans.points,
    trans.theTable,
    trans.invite,
    trans.casual,
    trans.rated
  )
}
