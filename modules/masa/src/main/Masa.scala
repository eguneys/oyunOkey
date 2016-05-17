package oyun.masa

import org.joda.time.{ DateTime }
import ornicar.scalalib.Random

import oyun.game.PerfPicker

case class Masa(
  id: String,
  name: String,
  status: Status,
  system: System,
  clock: MasaClock,
  rounds: Int,
  variant: okey.variant.Variant,
  mode: oyun.game.Mode,
  allowAnon: Boolean,
  nbPlayers: Int,
  nbRounds: Int,
  createdAt: DateTime,
  createdBy: String,
  winnerId: Option[String] = None,
  spotlight: Option[Spotlight] = None) {

  def isCreated = status == Status.Created
  def isStarted = status == Status.Started
  def isFinished = status == Status.Finished

  def rated = mode.rated

  def membersOnly = !allowAnon || rated

  def fullName =
    s"$name $system"

  def roundString =
    s"$nbRounds/$rounds " + "el"

  def isRecentlyCreated = isCreated && (nowSeconds - createdAt.getSeconds) < 60

  def roundsToFinish = (rounds - nbRounds) max 0

  def isAlmostFinished = roundsToFinish == 0

  def ratingVariant = (variant == okey.variant.StandardTest).fold(okey.variant.Standard, variant)

  def perfType = PerfPicker.perfType(ratingVariant)
  def perfLens = PerfPicker.mainOrDefault(ratingVariant)

  def createPairings(masa: Masa, players: List[String]): Fu[Option[Pairing]] = {
    fuccess(for {
      prep <- makePrep(masa, players)
      pairing = prep.toPairing
    } yield pairing)
  }

  private def makePrep(masa: Masa, players: List[String]): Option[Pairing.Prep] = {
    players match {
      case List(p1, p2, p3, p4) => Some(Pairing.prep(masa, p1, p2, p3, p4))
      case _ => None
    }
  }
}

object Masa {

  def make(
    createdByUserId: String,
    clock: MasaClock,
    rounds: Int,
    system: System,
    variant: okey.variant.Variant,
    mode: oyun.game.Mode,
    allowAnon: Boolean) = Masa(
    id = Random nextStringUppercase 8,
      name = GreatPlayer.randomName,
      status = Status.Created,
      system = system,
      clock = clock,
      rounds = rounds,
      createdBy = createdByUserId,
      createdAt = DateTime.now,
      nbPlayers = 0,
      nbRounds = 0,
      variant = variant,
      mode = mode,
      allowAnon = allowAnon)
}
