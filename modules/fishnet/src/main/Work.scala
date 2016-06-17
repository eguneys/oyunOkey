package oyun.fishnet

import org.joda.time.DateTime

import oyun.game.{ Game => OGame }

sealed trait Work {
  def _id: Work.Id
  def game: Work.Game
  def createdAt: DateTime
  def acquired: Option[Work.Acquired]

  def id = _id

  def isAcquiredBy(client: Client) = true
  def isAcquired = acquired.isDefined
  def nonAcquired = !isAcquired
}


object Work {

  case class Id(value: String) extends AnyVal

  case class Acquired(date: DateTime)

  case class Game(game: OGame) {
    def id = game.id
  }

  case class Move(
    _id: Work.Id,
    game: Game,
    level: Int,
    acquired: Option[Acquired],
    createdAt: DateTime) extends Work {

    def assignTo(client: Client) = copy(
      acquired = Acquired(
        date = DateTime.now).some
    )

    def similar(to: Move) = game.id == to.game.id && game.game.turns == to.game.game.turns
  }

  def makeId = Id(scala.util.Random.alphanumeric take 8 mkString)

}
