package oyun

import oyun.game.Event
import oyun.socket.WithSocket

package object round extends PackageObject with WithPlay with WithSocket {
  private[round]type Events = List[Event]

  private[round]type VersionedEvents = List[VersionedEvent]
}

package round {
  private [round] class ClientErrorException(e: String) extends Exception(e)

  private[round] object ClientErrorException {

    def future (e: String) = fufail(new ClientErrorException(e))

  }
}
