package views
package html.masa

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._

import controllers.routes

object side {

  private val separator = " • ";

  def apply(m: oyun.masa.Masa)
    (implicit ctx: Context) = frag(
    div(cls := "masa__meta")(
      st.section(dataIcon := m.perfType.map(_.iconChar.toString))(
        div(
          views.html.game.bits.variantLink(
            m.variant,
            m.variant.name),
          p(m.roundString),
          m.mode.fold(trans.casualTable, trans.ratedTable)(),
          separator,
          systemName(m.system).capitalize
        )
      ),
      frag(trans.by(usernameOrAnon(m.createdBy.some)), br),
      absClientDateTime(m.createdAt)
    ),
      views.html.chat.frag
  )

}
