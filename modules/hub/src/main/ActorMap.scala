package oyun.hub

import akka.actor._
import akka.pattern.{ ask }

import actorApi.map._

trait ActorMap extends Actor {

  def tell(id: String, msg: Any): Unit = getOrMake(id) ! msg

  private val actors = scala.collection.mutable.Map.empty[String, ActorRef]

  def mkActor(id: String): Actor

  def actorMapReceive: Receive = {

    case Get(id) => sender ! getOrMake(id)

    case Tell(id, msg) => getOrMake(id) forward msg

    case Ask(id, msg) => getOrMake(id) forward msg

    case Terminated(actor) =>
      context unwatch actor
      actors foreach {
        case (id, a) => if (a == actor) actors -= id
      }
  }

  def size = actors.size

  private def getOrMake(id: String) = actors get id getOrElse {
    context.actorOf(Props(mkActor(id)), name = id) ~ { actor =>
      actors += (id -> actor)
      context watch actor
    }
  }
}

object ActorMap {
  def apply(make: String => Actor) = new ActorMap {
    def mkActor(id: String) = make(id)
    def receive = actorMapReceive
  }
}
