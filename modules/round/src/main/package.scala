package oyun

import oyun.socket.WithSocket

package object round extends PackageObject with WithPlay with WithSocket {
  private[round]type VersionedEvents = List[VersionedEvent]
}
