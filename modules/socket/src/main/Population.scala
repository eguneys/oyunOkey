package oyun.socket

import akka.actor._

import oyun.hub.Trouper
import actorApi.{ SocketEnter, SocketLeave, PopulationTell, NbMembers }

private[socket] final class Population(system: akka.actor.ActorSystem) extends Trouper {

  var nb = 0
  val bus = system.oyunBus

  bus.subscribe(this, 'socketEnter, 'socketLeave)

  // override def postStop() {
  //   super.postStop()
  //   bus.unsubscribe(self)
  // }

  val process: Trouper.Receive = {

    case _: SocketEnter[_] =>
      nb = nb + 1

    case _: SocketLeave[_] =>
      nb = nb - 1

    case PopulationTell => bus.publish(NbMembers(nb), 'nbMembers)
  }
}
