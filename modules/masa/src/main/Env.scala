package oyun.masa

import akka.actor._
import akka.pattern.ask
import com.typesafe.config.Config
import scala.concurrent.duration._

import oyun.common.PimpedConfig._
import oyun.hub.actorApi.map.Ask
import oyun.hub.{ ActorMap, Sequencer }
import oyun.socket.Socket.{ GetVersion }
import oyun.socket.History
import makeTimeout.short

final class Env(
  config: Config,
  system: ActorSystem,
  db: oyun.db.Env,
  mongoCache: oyun.memo.MongoCache.Builder,
  asyncCache: oyun.memo.AsyncCache.Builder,
  hub: oyun.hub.Env,
  lightUser: String => Option[oyun.common.LightUser],
  rankingApi: oyun.user.RankingApi,
  isOnline: String => Boolean,
  scheduler: oyun.common.Scheduler) {

  private val settings = new {
    val CollectionMasa = config getString "collection.masa"
    val CollectionPairing = config getString "collection.pairing"
    val CollectionPlayer = config getString "collection.player"
    val HistoryMessageTtl = config duration "history.message.ttl"
    val CreatedCacheTtl = config duration "created.cache.ttl"
    val LeaderboardCacheTtl = config duration "leaderboard.cache.ttl"
    val SocketTimeout = config duration "socket.timeout"
    val SocketName = config getString "socket.name"
    val ApiActorName = config getString "api_actor.name"
    val SequencerTimeout = config duration "sequencer.timeout"
  }
  import settings._

  lazy val forms = new DataForm


  lazy val cached = new Cached(
    asyncCache = asyncCache,
    createdTtl = CreatedCacheTtl)(system)

  // private def isAnonOnline(masaId: String, player: Player) =
  //   player.isRecentlyCreated.fold(fuccess(true), {
  //     socketHub ? Ask(masaId, actorApi.GetWaitingPlayers) mapTo manifest[Set[String]] map (_.exists(player.id==))
  //   })

  // private def isPlayerOnline(masaId: String)(player: Player) = player.userId.fold(isAnonOnline(masaId, player))(funit inject isOnline(_))

  lazy val api = new MasaApi(
    scheduleJsonView = scheduleJsonView,
    system = system,
    sequencers = sequencerMap,
    socketMap = socketMap,
    autoPairing = autoPairing,
    perfsUpdater = perfsUpdater,
    renderer = hub.actor.renderer,
    site = hub.socket.site,
    lobby = hub.socket.lobby,
    asyncCache = asyncCache)

  val masa = api masa _

  lazy val perfsUpdater = new PerfsUpdater(rankingApi)

  lazy val socketHandler = new SocketHandler(
    hub = hub,
    socketMap = socketMap,
    chat = hub.actor.chat)

  lazy val winners = new Winners(
    mongoCache = mongoCache,
    ttl = LeaderboardCacheTtl)

  lazy val jsonView = new JsonView(lightUser, asyncCache)

  lazy val scheduleJsonView = new ScheduleJsonView(lightUser)

  // private val socketHub = system.actorOf(Props(new oyun.socket.SocketHubActor.Default[Socket] {
  //   def mkActor(masaId: String) = new Socket(
  //     masaId = masaId,
  //     history = new History(ttl = HistoryMessageTtl),
  //     socketTimeout = SocketTimeout)
  // }), name = SocketName)

  private val socketMap: SocketMap = oyun.socket.SocketMap[MasaSocket](
    system = system,
    mkTrouper = (masaId: String) => new MasaSocket(
      system = system,
      masaId = masaId,
      history = new History(ttl = HistoryMessageTtl),
      uidTtl = SocketTimeout),
    accessTimeout = SocketTimeout,
    monitoringName = "masa.socketMap",
    broomFrequency = 3701 millis
  )

  private val sequencerMap = system.actorOf(Props(ActorMap { id =>
    new Sequencer(
      receiveTimeout = SequencerTimeout.some,
      executionTimeout = 5.seconds.some,
      logger = logger)
  }))

  system.oyunBus.subscribe(
    system.actorOf(Props(new ApiActor(api = api)), name = ApiActorName),
    'finishGame, 'withdrawMasa)


  system.actorOf(Props(new CreatedOrganizer(
    api = api,
    isOnline = isOnline
  )))

  system.actorOf(Props(new InterruptedOrganizer(api = api)))

  private val reminder = system.actorOf(Props(new Reminder(
    renderer = hub.actor.renderer
  )))

  system.actorOf(Props(new StartedOrganizer(
    api = api,
    reminder = reminder,
    isOnline = isOnline,
    socketMap = socketMap
  )))

  def version(masaId: Masa.ID): Fu[Int] =
    socketMap.askIfPresentOrZero[Int](masaId)(GetVersion)

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
    mongoCache = oyun.memo.Env.current.mongoCache,
    asyncCache = oyun.memo.Env.current.asyncCache,
    db = oyun.db.Env.current,
    hub = oyun.hub.Env.current,
    lightUser = oyun.user.Env.current.lightUser,
    rankingApi = oyun.user.Env.current.rankingApi,
    isOnline = oyun.user.Env.current.isOnline,
    scheduler = oyun.common.PlayApp.scheduler
  )
}

