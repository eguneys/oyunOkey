package oyun.forum

import akka.actor._
import com.typesafe.config.Config

import oyun.common.{ MaxPerPage }

final class Env(
  config: Config,
  db: oyun.db.Env, 
  hub: oyun.hub.Env,
  system: ActorSystem) {

  private val settings = new {
    val TopicMaxPerPage = config getInt "topic.max_per_page"
    val PostMaxPerPage = config getInt "post.max_per_page"
    val CollectionCateg = config getString "collection.categ"
    val CollectionTopic = config getString "collection.topic"
    val CollectionPost = config getString "collection.post"
  }
  import settings._

  lazy val categApi = new CategApi(env = this)

  lazy val topicApi = new TopicApi(
    env = this,
    maxPerPage = MaxPerPage(TopicMaxPerPage),
    bus = system.oyunBus)

  lazy val postApi = new PostApi(
    env = this,
    maxPerPage = MaxPerPage(TopicMaxPerPage),
    bus = system.oyunBus
  )

  lazy val forms = new DataForm(hub.captcher)

  private[forum] lazy val categColl = db(CollectionCateg)
  private[forum] lazy val topicColl = db(CollectionTopic)
  private[forum] lazy val postColl = db(CollectionPost)
}

object Env {

  lazy val current = "forum" boot new Env(
    config = oyun.common.PlayApp loadConfig "forum",
    db = oyun.db.Env.current,
    hub = oyun.hub.Env.current,
    system = oyun.common.PlayApp.system
  )

}
