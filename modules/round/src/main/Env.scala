package oyun.round

import akka.actor._
import com.typesafe.config.Config

import actorApi.{ GetSocketStatus, SocketStatus }

final class Env(
  config: Config,
  system: ActorSystem) {

  private val settings = new {
    val SocketName = config getString "socket.name"
  }
  import settings._

  private val socketHub = {
    val actor = system.actorOf(
      Props(new oyun.socket.SocketHubActor[Socket] {
        def mkActor(id: String) = new Socket(
        )
        def receive: Receive = socketHubReceive
      }),
      name = SocketName)
    actor
  }

  lazy val socketHandler = new SocketHandler(socketHub = socketHub)

  private def getSocketStatus(gameId: String): Fu[SocketStatus] =
    fuccess(SocketStatus(1))

  lazy val jsonView = new JsonView(getSocketStatus)
}

object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp loadConfig "round",
    system = oyun.common.PlayApp.system)
}
