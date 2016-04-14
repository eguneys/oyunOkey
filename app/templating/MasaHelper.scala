package oyun.app
package templating

import oyun.masa.{ Masa }
import oyun.user.{ UserContext }

trait MasaHelper { self: I18nHelper => 

  def masaIconChar(masa: Masa): Char = 'g'

  def masaRoundString(masa: Masa)(implicit ctx: UserContext) = s"${masa.roundString}${trans.rounds().toString.head}"

}
