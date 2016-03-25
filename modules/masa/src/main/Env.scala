package oyun.masa

import akka.actor._
import akka.pattern.ask
import com.typesafe.config.Config

import oyun.common.PimpedConfig._
import oyun.hub.actorApi.map.Ask
import oyun.socket.actorApi.GetVersion
import oyun.socket.History
import makeTimeout.short

import oyun.memo.{ ExpireSetMemo }

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
    val SocketName = config getString "socket.name"
    val OrganizerName = config getString "organizer.name"
  }
  import settings._


  private def isPlayerOnline(player: Player) = true

  private val socketHub = system.actorOf(Props(new oyun.socket.SocketHubActor.Default[Socket] {
    def mkActor(masaId: String) = new Socket(
      masaId = masaId,
      history = new History()
    )
  }), name = SocketName)


  lazy val socketHandler = new SocketHandler(
    hub = hub,
    socketHub = socketHub)

  lazy val jsonView = new JsonView()

  lazy val api = new MasaApi(
    socketHub = socketHub,
    autoPairing = autoPairing)

  val masa = api masa _

  private val organizer = system.actorOf(Props(new Organizer(
    api = api,
    isOnline = isPlayerOnline,
    socketHub = socketHub
  )), name = OrganizerName)

  def version(masaId: String): Fu[Int] =
    socketHub ? Ask(masaId, GetVersion) mapTo manifest[Int]

  private lazy val autoPairing = new AutoPairing(
  )

  {
    import scala.concurrent.duration._

    scheduler.message(2 seconds) {
      organizer -> actorApi.AllCreatedMasas
    }

    scheduler.message(3 seconds) {
      organizer -> actorApi.StartedMasas
    }
  }

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

