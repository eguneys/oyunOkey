package oyun.socket

import akka.actor._
import akka.pattern.{ ask, pipe }

final class SocketHub extends Actor {
  private val sockets = collection.mutable.Set[ActorRef]()

  //context.system.lilaBus.subscribe(self, 'socket)

  import SocketHub._

  def receive = {
    case Open(socket) => sockets += socket
    case Close(socket) => sockets -= socket
    case msg => sockets foreach (_ ! msg)
  }
}

case object SocketHub {
  case class Open(actor: ActorRef)
  case class Close(actor: ActorRef)
}
