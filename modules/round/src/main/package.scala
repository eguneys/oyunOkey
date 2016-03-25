package oyun

import oyun.game.Event
import oyun.socket.WithSocket

package object round extends PackageObject with WithPlay with WithSocket {
  private[round]type Events = List[Event]

  private[round]type VersionedEvents = List[VersionedEvent]
}

package round {

  private [round] sealed trait BenignError extends oyun.common.OyunException
  private [round] case class ClientError(message: String) extends BenignError
}
