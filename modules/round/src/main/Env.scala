package oyun.round

import akka.actor._
import akka.pattern.{ ask }
import com.typesafe.config.Config

import actorApi.{ GetSocketStatus, SocketStatus }
import oyun.common.PimpedConfig._
import oyun.hub.actorApi.map.{ Ask, Tell }
import makeTimeout.large

final class Env(
  config: Config,
  system: ActorSystem) {

  private val settings = new {
    val PlayerDisconnectTimeout = config duration "player.disconnect.timeout"
    val PlayerRagequitTimeout = config duration "player.ragequit.timeout"
    val SocketTimeout = config duration "socket.timeout"
    val SocketName = config getString "socket.name"
    val ActorMapName = config getString "actor.map.name"
  }
  import settings._

  lazy val eventHistory = History() _

  val roundMap = system.actorOf(Props(new oyun.hub.ActorMap {
    def mkActor(id: String) = new Round(
      gameId = id,
      player = player,
      socketHub
    )

    def receive: Receive = actorMapReceive
  }), name = ActorMapName)

  private val socketHub = {
    val actor = system.actorOf(
      Props(new oyun.socket.SocketHubActor[Socket] {
        def mkActor(id: String) = new Socket(
          gameId = id,
          history = eventHistory(id),
          socketTimeout = SocketTimeout,
          disconnectTimeout = PlayerDisconnectTimeout,
          ragequitTimeout = PlayerRagequitTimeout)
        def receive: Receive = socketHubReceive
      }),
      name = SocketName)
    actor
  }

  lazy val socketHandler = new SocketHandler(
    roundMap = roundMap,
    socketHub = socketHub
  )

  private lazy val finisher = new Finisher(
    bus = system.oyunBus)

  private lazy val player: Player = new Player(
    finisher = finisher
  )

  private def getSocketStatus(gameId: String): Fu[SocketStatus] =
    socketHub ? Ask(gameId, GetSocketStatus) mapTo manifest[SocketStatus]

  lazy val jsonView = new JsonView(getSocketStatus)
}

object Env {
  lazy val current = "round" boot new Env(
    config = oyun.common.PlayApp loadConfig "round",
    system = oyun.common.PlayApp.system)
}
