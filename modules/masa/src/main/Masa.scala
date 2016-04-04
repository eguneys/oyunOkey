package oyun.masa

import ornicar.scalalib.Random

case class Masa(
  id: String,
  status: Status,
  system: System) {

  def isCreated = status == Status.Created
  def isStarted = status == Status.Started
  def isFinished = status == Status.Finished

  def fullName =
    s"name"


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

  def make(system: System) = Masa(
    id = Random nextStringUppercase 8,
    status = Status.Created,
    system = system)
}
