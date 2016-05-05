package oyun

import oyun.game.Event
import oyun.socket.WithSocket

package object round extends PackageObject with WithPlay with WithSocket {
  private[round]type Events = List[Event]

  private[round]type VersionedEvents = List[VersionedEvent]

  private[round] def logger = oyun.log("round")
}

package round {

  private [round] sealed trait BenignError extends oyun.common.OyunException
  private [round] case class ClientError(message: String) extends BenignError
  private [round] case class FishnetError(message: String) extends BenignError
}
