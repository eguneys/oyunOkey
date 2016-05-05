package oyun.fishnet

import akka.actor._
import akka.pattern.ask
import org.joda.time.DateTime

import oyun.hub.{ actorApi => hubApi }
import makeTimeout.short

private final class MoveDB(
  roundMap: ActorSelection,
  system: ActorSystem) {

  import Work.Move

  def add(move: Move) = actor ! Add(move)

  def acquire(client: Client): Fu[Option[Move]] =
    actor ? Acquire(client) mapTo manifest[Option[Move]]

  def postResult(
    moveId: Work.Id,
    client: Client,
    data: Option[okey.format.Uci]) =
    actor ! PostResult(moveId, client, data)

  private case class Add(move: Move)
  private case class Acquire(client: Client)
  private case class PostResult(
    moveId: Work.Id,
    client: Client,
    data: Option[okey.format.Uci])

  private val actor = system.actorOf(Props(new Actor {
    val coll = scala.collection.mutable.Map.empty[Work.Id, Move]

    val maxSize = 300

    def receive = {

      case Add(move) =>
        clearIfFull
        coll += (move.id -> move)

      case Acquire(client) => sender ! coll.values.foldLeft(none[Move]) {
        case (found, m) if m.nonAcquired => Some {
          found.fold(m) { a =>
            a
          }
        }
        case (found, _) => found
      }.map { m =>
        val move = m assignTo client
        coll += (move.id -> move)
        move
      }

      case PostResult(moveId, client, data) =>
        coll get moveId match {
          case None => println(s"move not found ${moveId}")
          case Some(move) if move isAcquiredBy client => data match {
            case Some(uci) =>
              coll -= move.id
              roundMap ! hubApi.map.Tell(move.game.id, hubApi.round.FishnetPlay(uci))
            case _ =>
              println(s"move invalid ${moveId}")
          }
          case Some(move) => println(s"move not acquired ${moveId}")
        }
    }

    def clearIfFull =
      if (coll.size > maxSize) {
        logger.warn(s"MoveDB collection is full! maxSize=$maxSize. Dropping all now!")
        coll.clear()
      }
  }))
  
}
