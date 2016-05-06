package oyun.chat

import akka.actor._
import com.typesafe.config.Config

import oyun.common.PimpedConfig._

final class Env(
  config: Config,
  db: oyun.db.Env,
  flood: oyun.security.Flood,
  system: ActorSystem) {

  private val settings = new {
    val CollectionChat = config getString "collection.chat"
    val MaxLinesPerChat = config getInt "max_lines"
    val NetDomain = config getString "net.domain"
    val ActorName = config getString "actor.name"
  }
  import settings._

  lazy val api = new ChatApi(
    coll = chatColl,
    flood = flood,
    maxLinesPerChat = MaxLinesPerChat,
    netDomain = NetDomain)

  system.actorOf(Props(new FrontActor(api)), name = ActorName)

  private[chat] lazy val chatColl = db(CollectionChat)
}


object Env {

  lazy val current: Env = "chat" boot new Env(
    config = oyun.common.PlayApp loadConfig "chat",
    db = oyun.db.Env.current,
    flood = oyun.security.Env.current.flood,
    system = oyun.common.PlayApp.system)
}
