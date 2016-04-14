package oyun.masa

import akka.actor._
import akka.pattern.ask
import com.typesafe.config.Config
import scala.concurrent.duration._

import oyun.common.PimpedConfig._
import oyun.hub.actorApi.map.Ask
import oyun.hub.{ ActorMap, Sequencer }
import oyun.socket.actorApi.GetVersion
import oyun.socket.History
import makeTimeout.short

final class Env(
  config: Config,
  system: ActorSystem,
  db: oyun.db.Env,
  hub: oyun.hub.Env,
  scheduler: oyun.common.Scheduler) {

  private val settings = new {
    val CollectionMasa = config getString "collection.masa"
    val CollectionPairing = config getString "collection.pairing"
    val CollectionPlayer = config getString "collection.player"
    val CreatedCacheTtl = config duration "created.cache.ttl"
    val SocketTimeout = config duration "socket.timeout"
    val SocketName = config getString "socket.name"
    val ApiActorName = config getString "api_actor.name"
    val SequencerTimeout = config duration "sequencer.timeout"
  }
  import settings._


  lazy val cached = new Cached(
    createdTtl = CreatedCacheTtl)

  private def isPlayerOnline(player: Player) = true

  lazy val api = new MasaApi(
    sequencers = sequencerMap,
    socketHub = socketHub,
    autoPairing = autoPairing)

  val masa = api masa _

  lazy val socketHandler = new SocketHandler(
    hub = hub,
    socketHub = socketHub)

  lazy val jsonView = new JsonView()

  lazy val scheduleJsonView = new ScheduleJsonView()

  private val socketHub = system.actorOf(Props(new oyun.socket.SocketHubActor.Default[Socket] {
    def mkActor(masaId: String) = new Socket(
      masaId = masaId,
      history = new History(),
      socketTimeout = SocketTimeout)
  }), name = SocketName)

  private val sequencerMap = system.actorOf(Props(ActorMap { id =>
    new Sequencer(
      receiveTimeout = SequencerTimeout.some,
      executionTimeout = 5.seconds.some,
      logger = logger)
  }))

  system.oyunBus.subscribe(
    system.actorOf(Props(new ApiActor(api = api)), name = ApiActorName),
    'finishGame)


  system.actorOf(Props(new CreatedOrganizer(
    api = api,
    isOnline = isPlayerOnline
  )))

  system.actorOf(Props(new StartedOrganizer(
    api = api,
    isOnline = isPlayerOnline,
    socketHub = socketHub
  )))

  def version(masaId: String): Fu[Int] =
    socketHub ? Ask(masaId, GetVersion) mapTo manifest[Int]

  private lazy val autoPairing = new AutoPairing(
  )

  private[masa] lazy val masaColl = db(CollectionMasa)
  private[masa] lazy val pairingColl = db(CollectionPairing)
  private[masa] lazy val playerColl = db(CollectionPlayer)
}


object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp loadConfig "masa",
    system = oyun.common.PlayApp.system,
    db = oyun.db.Env.current,
    hub = oyun.hub.Env.current,
    scheduler = oyun.common.PlayApp.scheduler
  )
}

