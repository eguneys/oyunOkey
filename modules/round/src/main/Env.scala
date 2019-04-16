package oyun.round

import akka.actor._
import akka.pattern.{ ask }
import com.typesafe.config.Config
import scala.concurrent.duration._

import oyun.game.{ Game }
import actorApi.{ GetSocketStatus, SocketStatus }
import oyun.common.PimpedConfig._
import oyun.hub.actorApi.map.{ Ask, Tell }
import makeTimeout.large

final class Env(
  config: Config,
  system: ActorSystem,
  hub: oyun.hub.Env,
  fishnetPlayer: oyun.fishnet.Player,
  userJsonView: oyun.user.JsonView,
  chatApi: oyun.chat.ChatApi) {

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

  def roundProxyGame(gameId: Game.ID): Fu[Option[Game]] =
    roundMap.getOrMake(gameId).getGame addEffect { g =>
      if (!g.isDefined) roundMap kill gameId
    }

  private def scheduleExpiration(game: Game): Unit = game.timeBeforeExpiration foreach { centis =>
    system.scheduler.scheduleOnce((centis.millis + 1000).millis) {
      roundMap.tell(game.id, actorApi.round.NoStart)
    }
  }

  val roundMap = new oyun.hub.DuctMap[Round](
    mkDuct = id => {
      val duct = new Round(gameId = id,
        finisher = finisher,
        player = player,
        socketHub,
        activeTtl = ActiveTtl,
        bus = system.oyunBus)
      duct.getGame foreach { _ foreach scheduleExpiration }
      duct
    },
    accessTimeout = ActiveTtl
  )

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

  lazy val perfsUpdater = new PerfsUpdater()

  lazy val socketHandler = new SocketHandler(
    roundMap = roundMap,
    socketHub = socketHub,
    messenger = messenger
  )

  private lazy val finisher = new Finisher(
    perfsUpdater = perfsUpdater,
    bus = system.oyunBus)

  private lazy val player: Player = new Player(
    fishnetPlayer = fishnetPlayer,
    finisher = finisher
  )

  lazy val messenger = new Messenger(
    socketHub = socketHub,
    chat = hub.actor.chat
  )

  private def getSocketStatus(gameId: String): Fu[SocketStatus] =
    socketHub ? Ask(gameId, GetSocketStatus) mapTo manifest[SocketStatus]

  lazy val jsonView = new JsonView(
    chatApi = chatApi,
    userJsonView = userJsonView,
    getSocketStatus = getSocketStatus)

  system.scheduler.schedule(5 seconds, 2 seconds) {
    nbRounds = roundMap.size
    system.oyunBus.publish(oyun.hub.actorApi.round.NbRounds(nbRounds), 'nbRounds)
  }

  // WIP
  // system.actorOf(
  //   Props(classOf[Titivate], roundMap),
  //   name = "Titivate")
}

object Env {
  lazy val current = "round" boot new Env(
    config = oyun.common.PlayApp loadConfig "round",
    system = oyun.common.PlayApp.system,
    hub = oyun.hub.Env.current,
    fishnetPlayer = oyun.fishnet.Env.current.player,
    userJsonView = oyun.user.Env.current.jsonView,
    chatApi = oyun.chat.Env.current.api)
}
