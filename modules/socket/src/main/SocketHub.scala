// package oyun.socket

// import akka.actor._
// import akka.pattern.{ ask, pipe }

// final class SocketHub extends Actor {
//   private val sockets = collection.mutable.Set[ActorRef]()

//   override def preStart() {
//     context.system.oyunBus.subscribe(self, 'deploy, 'socket)
//   }

//   override def postStop() {
//     super.postStop()
//     context.system.oyunBus.unsubscribe(self)
//   }

//   import SocketHub._

//   def receive = {
//     case Open(socket) => sockets += socket
//     case Close(socket) => sockets -= socket
//     case msg => sockets foreach (_ ! msg)
//   }
// }

// case object SocketHub {
//   case class Open(actor: ActorRef)
//   case class Close(actor: ActorRef)
// }
