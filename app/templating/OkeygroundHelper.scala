package oyun.app
package templating

import okey.{ Side, Table, Pos }
import oyun.api.Context

import oyun.app.ui.ScalatagsTemplate._
import oyun.game.Pov

trait OkeygroundHelper {

  private val cgWrap = div(cls := "cg-wrap")
  private val cgHelper = tag("cg-helper")
  private val cgContainer = tag("cg-container")
  private val cgBoard = tag("cg-board")
  private val cgWrapContent = cgHelper(cgContainer(cgBoard))

  def okeyground(table: Table, orient: Side)(implicit ctx: Context): Frag = wrap {
    cgBoard {
      raw {
        ""
      }
    }
  }

  def okeyground(pov: Pov)(implicit ctx: Context): Frag = okeyground(
    table = pov.game.table,
    orient = pov.side
  )

  private def wrap(content: Frag): Frag = cgWrap {
    cgHelper {
      cgContainer {
        content
      }
    }
  }

}
