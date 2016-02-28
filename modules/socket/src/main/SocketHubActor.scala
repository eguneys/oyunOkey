package oyun.socket

import oyun.hub.ActorMap

trait SocketHubActor[A <: SocketActor[_]] extends Socket with ActorMap {
  def socketHubReceive: Receive = actorMapReceive
}
