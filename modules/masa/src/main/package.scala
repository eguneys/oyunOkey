package oyun

import oyun.socket.WithSocket

package object masa extends PackageObject with WithPlay with WithSocket {

  private[masa] type SocketMap = oyun.hub.TrouperMap[masa.MasaSocket]

  private[masa] type Players = List[masa.Player]

  private[masa] type RankedPlayers = List[RankedPlayer]

  private[masa] type Pairings = List[masa.Pairing]

  private[masa] val logger = oyun.log("masa")

  private[masa] val pairingLogger = logger branch "pairing"
}
