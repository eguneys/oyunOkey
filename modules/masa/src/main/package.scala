package oyun

import oyun.socket.WithSocket

package object masa extends PackageObject with WithPlay with WithSocket {

  private[masa]type Players = List[masa.Player]

  private[masa]type RankedPlayers = List[RankedPlayer]

  private[masa]type Pairings = List[masa.Pairing]
}
