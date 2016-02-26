package oyun.lobby

import akka.actor._
import com.typesafe.config.Config

import oyun.socket.History

final class Env(
  config: Config,
  system: ActorSystem) {
  private val settings = new {
    val SocketName = config getString "socket.name"
    val ActorName = config getString "actor.name"
  }
  import settings._

  private val socket = system.actorOf(Props(new Socket(
    history = history
  )), name = SocketName)

  val lobby = system.actorOf(Props(new Lobby(
    socket = socket)), name = ActorName)


  lazy val socketHandler = new SocketHandler(
    socket = socket)

  lazy val history = new History()
}


object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp loadConfig "lobby",
    system = oyun.common.PlayApp.system
  )
}
