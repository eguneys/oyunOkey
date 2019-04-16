package oyun.app

import akka.actor._
import com.typesafe.config.Config

final class Env(
  config: Config,
  system: ActorSystem) {

  private val RendererName = config getString "app.renderer.name"

  lazy val bus = oyun.common.Bus(system)

  lazy val preloader = new mashup.Preload(
    countRounds = Env.round.count,
    lobbyApi = Env.api.lobbyApi)

  lazy val userInfo = mashup.UserInfo(
    countUsers = () => Env.user.countEnabled,
    gameCached = Env.game.cached) _

  system.actorOf(Props(new actor.Renderer), name = RendererName)

  oyun.log.boot.info("Preloading modules")
  oyun.common.Chronometer.syncEffect(List(Env.socket,
    Env.site,
    Env.masa,
    Env.lobby,
    Env.round,
    Env.pref,
    Env.chat,
    Env.fishnet
  )) { lap =>
    oyun.log("boot").info(s"${lap.millis}ms Preloading complete")
  }
}

object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp.loadConfig,
    system = oyun.common.PlayApp.system)

  def api = oyun.api.Env.current
  def db = oyun.db.Env.current
  def user = oyun.user.Env.current
  def i18n = oyun.i18n.Env.current
  def security =  oyun.security.Env.current
  def hub = oyun.hub.Env.current
  def fishnet = oyun.fishnet.Env.current
  def socket = oyun.socket.Env.current
  def setup = oyun.setup.Env.current
  def site = oyun.site.Env.current
  def lobby = oyun.lobby.Env.current
  def round = oyun.round.Env.current
  def game = oyun.game.Env.current
  def masa = oyun.masa.Env.current
  def forum = oyun.forum.Env.current
  // def site = oyun.site.Env.current
  def pref = oyun.pref.Env.current
  def chat = oyun.chat.Env.current
}

