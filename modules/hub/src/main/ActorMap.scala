package oyun.hub

import akka.actor._
import akka.pattern.{ ask }

import actorApi.map._

trait ActorMap extends Actor {

  private val actors = scala.collection.mutable.Map.empty[String, ActorRef]

  def mkActor(id: String): Actor

  def actorMapReceive: Receive = {
    case Get(id) => sender ! getOrMake(id)
  }

  private def getOrMake(id: String) = actors get id getOrElse {
    context.actorOf(Props(mkActor(id)), name = id) ~ { actor =>
      actors += (id -> actor)
      context watch actor
    }
  }
}
