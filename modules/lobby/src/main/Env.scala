package oyun.lobby

import akka.actor._
import com.typesafe.config.Config

import oyun.common.PimpedConfig._
import oyun.socket.History

final class Env(
  config: Config,
  system: ActorSystem,
  db: oyun.db.Env,
  hub: oyun.hub.Env,
  asyncCache: oyun.memo.AsyncCache.Builder,
  scheduler: oyun.common.Scheduler) {
  private val settings = new {
    val MessageTtl = config duration "message.ttl"
    val SocketUidTtl = config duration "socket.uid.ttl"
    val SocketName = config getString "socket.name"
    val ActorName = config getString "actor.name"
    val BroomPeriod = config duration "broom_period"
    val CollectionSeek = config getString "collection.seek"
    val SeekMaxPerPage = config getInt "seek.max_per_page"
    val SeekMaxPerUser = config getInt "seek.max_per_user"
  }
  import settings._

  lazy val seekApi = new SeekApi(
    coll = db(CollectionSeek),
    asyncCache = asyncCache,
    maxPerPage = SeekMaxPerPage,
    maxPerUser = SeekMaxPerUser
  )

  private val socket = new LobbySocket(system, SocketUidTtl)

  // val lobby = system.actorOf(Props(new Lobby(
  //   socket = socket)), name = ActorName)

  private val lobbyTrouper = LobbyTrouper.start(
    system) { () =>
    new LobbyTrouper(
      system = system,
      socket = socket)
  }

  lazy val socketHandler = new SocketHandler(
    hub = hub,
    lobby = lobbyTrouper,
    socket = socket)

  system.oyunBus.subscribe(socketHandler, 'nbMembers, 'nbRounds)


  lazy val history = new History(ttl = MessageTtl)

  // {
  //   import scala.concurrent.duration._

  //   scheduler.once(10 seconds) {
  //     scheduler.message(BroomPeriod) {
  //       lobby -> oyun.socket.actorApi.Broom
  //     }
  //   }
  // }
}


object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp loadConfig "lobby",
    system = oyun.common.PlayApp.system,
    db = oyun.db.Env.current,
    hub = oyun.hub.Env.current,
    asyncCache = oyun.memo.Env.current.asyncCache,
    scheduler = oyun.common.PlayApp.scheduler
  )
}
