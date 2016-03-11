package oyun

import oyun.socket.WithSocket

package object masa extends PackageObject with WithPlay with WithSocket {
  private[masa]type Pairings = List[masa.Pairing]
}
