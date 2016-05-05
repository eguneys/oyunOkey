package oyun.round

import akka.actor._
import akka.pattern.{ ask }
import com.typesafe.config.Config
import scala.concurrent.duration._

import actorApi.{ GetSocketStatus, SocketStatus }
import oyun.common.PimpedConfig._
import oyun.hub.actorApi.map.{ Ask, Tell }
import makeTimeout.large

final class Env(
  config: Config,
  system: ActorSystem,
  fishnetPlayer: oyun.fishnet.Player,
  userJsonView: oyun.user.JsonView,
  scheduler: oyun.common.Scheduler) {

  private val settings = new {
    val PlayerDisconnectTimeout = config duration "player.disconnect.timeout"
    val PlayerRagequitTimeout = config duration "player.ragequit.timeout"
    val SocketTimeout = config duration "socket.timeout"
    val SocketName = config getString "socket.name"
    val ActorMapName = config getString "actor.map.name"
    val ActiveTtl = config duration "active.ttl"
  }
  import settings._

  lazy val eventHistory = History() _

  val roundMap = system.actorOf(Props(new oyun.hub.ActorMap {
    def mkActor(id: String) = new Round(
      gameId = id,
      finisher = finisher,
      player = player,
      socketHub,
      activeTtl = ActiveTtl)
    def receive: Receive = ({
      case actorApi.GetNbRounds =>
        nbRounds = size
        system.oyunBus.publish(oyun.hub.actorApi.round.NbRounds(nbRounds), 'nbRounds)
    }: Receive) orElse actorMapReceive
  }), name = ActorMapName)

  private var nbRounds = 0
  def count() = nbRounds

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
    fishnetPlayer = fishnetPlayer,
    finisher = finisher
  )

  private def getSocketStatus(gameId: String): Fu[SocketStatus] =
    socketHub ? Ask(gameId, GetSocketStatus) mapTo manifest[SocketStatus]

  lazy val jsonView = new JsonView(
    userJsonView = userJsonView,
    getSocketStatus = getSocketStatus)

  scheduler.message(2.1 seconds)(roundMap -> actorApi.GetNbRounds)

  // WIP
  // system.actorOf(
  //   Props(classOf[Titivate], roundMap),
  //   name = "Titivate")
}

object Env {
  lazy val current = "round" boot new Env(
    config = oyun.common.PlayApp loadConfig "round",
    system = oyun.common.PlayApp.system,
    fishnetPlayer = oyun.fishnet.Env.current.player,
    userJsonView = oyun.user.Env.current.jsonView,
    scheduler = oyun.common.PlayApp.scheduler)
}
