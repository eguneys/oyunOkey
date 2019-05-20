package oyun.masa

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints

import oyun.common.Form._

import oyun.game.Mode

final class DataForm {

  import DataForm._

  def apply() = create fill MasaSetup(
    rounds = roundsDefault,
    variant = okey.variant.Standard.id,
    mode = none,
    allowAnon = true)


  private lazy val create = Form(
    mapping(
      "rounds" -> numberIn(roundChoices),
      "variant" -> number.verifying(validVariantIds contains _),
      "mode" -> optional(number.verifying(Mode.all map (_.id) contains _)),
      "allowAnon" -> boolean
    )(MasaSetup.apply)(MasaSetup.unapply)
  )  
}


object DataForm {

  import okey.variant._

  val rounds = (5 to 30 by 5) :+ 1
  val roundChoices = options(rounds, "%d round")
  val roundsDefault = 5

  val validVariants = oyun.common.PlayApp.isProd.fold(
    List(), List(StandardTest, DuzOkeyTest)) ::: List(Standard, DuzOkey)

  val validVariantIds = validVariants.map(_.id).toSet

}
