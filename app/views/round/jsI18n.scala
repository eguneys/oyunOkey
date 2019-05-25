package views.html.round

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.i18n.{ I18nKeys => trans }

object jsI18n {

  def apply(g: oyun.game.Game)(implicit ctx: Context) = i18nJsObject {
    baseTranslations
  }

  private val baseTranslations = Vector(
    trans.aiName,
    trans.viewMasa,
    trans.openSeries,
    trans.openPairs,
    trans.sortSeries,
    trans.sortPairs,
    trans.collectOpen,
    trans.leaveTaken,
    trans.gameEnded,
    trans.nbSecondsToPlayTheFirstMove
  )

}

