package oyun.fishnet

import akka.actor._
import com.typesafe.config.Config

final class Env(
  config: Config,
  system: ActorSystem,
  hub: oyun.hub.Env) {

  private val repo = new FishnetRepo()

  private val moveDb = new MoveDB(
    system = system
  )

  val player = new Player(moveDb = moveDb)

  val api = new FishnetApi(
    repo = repo,
    moveDb = moveDb
  )(system)

  // controller actor
  system.actorOf(Props(new ControllerActor(repo = repo, api = api)))
}


object Env {
  lazy val current: Env = "fishnet" boot new Env(
    config = oyun.common.PlayApp loadConfig "fishnet",
    system = oyun.common.PlayApp.system,
    hub = oyun.hub.Env.current
  )
}
