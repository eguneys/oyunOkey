package oyun.masa

import org.joda.time.{ DateTime }
import ornicar.scalalib.Random

case class Masa(
  id: String,
  name: String,
  status: Status,
  system: System,
  rounds: Int,
  variant: okey.variant.Variant,
  nbPlayers: Int,
  nbRounds: Int,
  createdAt: DateTime,
  createdBy: String,
  winnerId: Option[String] = None) {

  def isCreated = status == Status.Created
  def isStarted = status == Status.Started
  def isFinished = status == Status.Finished

  def fullName =
    s"$name $system"

  def roundString =
    s"$nbRounds/$rounds " + "el"

  def roundsToFinish = (rounds - nbRounds) max 0

  def isAlmostFinished = roundsToFinish == 0

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
    rounds: Int,
    system: System,
    variant: okey.variant.Variant) = Masa(
    id = Random nextStringUppercase 8,
      name = GreatPlayer.randomName,
      status = Status.Created,
      system = system,
      rounds = rounds,
      createdBy = createdByUserId,
      createdAt = DateTime.now,
      nbPlayers = 0,
      nbRounds = 0,
      variant = variant)
}
