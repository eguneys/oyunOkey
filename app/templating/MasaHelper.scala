package oyun.app
package templating

import controllers.routes

import oyun.api.Context
import oyun.masa.Env.{ current => masaEnv }
import oyun.masa.{ Masa }
import oyun.user.{ UserContext }

trait MasaHelper { self: I18nHelper with DateHelper with UserHelper => 

  def netBaseUrl: String

  def masaIconChar(masa: Masa): Char = 'g'

  def masaRoundString(masa: Masa)(implicit ctx: UserContext) = s"${masa.roundString}${trans.rounds().toString.head}"

  def masaIdToName(id: String) = masaEnv.cached name id getOrElse "Masa"

  private def longMasaDescription(masa: Masa)(implicit ctx: Context) =
    s"${masa.rounds} ellik ${showDate(masa.createdAt)} ${masa.fullName}. " +
      s"Şimdiye kadar ${masa.nbRounds} el oynandı. " +
      masa.winnerId.fold(s"Daha kazanan belli değil.") { winnerId =>
        s"${usernameOrAnon(winnerId.some)} ödülü evine götürdü!"
  }

  def masaOpenGraph(masa: Masa)(implicit ctx: Context) = oyun.app.ui.OpenGraph(
    title = s"${masa.fullName}: ${masa.variant.name} ${masa.roundString} #${masa.id}",
    url = s"$netBaseUrl${routes.Masa.show(masa.id).url}",
    description = longMasaDescription(masa))

}
