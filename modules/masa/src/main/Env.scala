package oyun.masa

import akka.actor._
import akka.pattern.ask
import com.typesafe.config.Config

import oyun.hub.actorApi.map.Ask
import oyun.socket.actorApi.GetVersion
import oyun.socket.History
import makeTimeout.short

final class Env(
  config: Config,
  system: ActorSystem,
  db: oyun.db.Env,
  hub: oyun.hub.Env) {

  private val settings = new {
    val CollectionMasa = config getString "collection.masa"
    val SocketName = config getString "socket.name"
  }
  import settings._

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

  lazy val api = new MasaApi()

  def version(masaId: String): Fu[Int] =
    socketHub ? Ask(masaId, GetVersion) mapTo manifest[Int]

  private[masa] lazy val masaColl = db(CollectionMasa)
}


object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp loadConfig "masa",
    system = oyun.common.PlayApp.system,
    db = oyun.db.Env.current,
    hub = oyun.hub.Env.current
  )
}

