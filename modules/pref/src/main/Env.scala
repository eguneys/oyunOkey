package oyun.pref

import com.typesafe.config.Config
import oyun.common.PimpedConfig._

final class Env(
  config: Config,
  bus: oyun.common.Bus,
  db: oyun.db.Env) {

  private val CollectionPref = config getString "collection.pref"
  private val CacheTtl = config duration "cache.ttl"

  lazy val api = new PrefApi(db(CollectionPref), CacheTtl, bus)

}

object Env {

  lazy val current = "pref" boot new Env(
    config = oyun.common.PlayApp loadConfig "pref",
    bus = oyun.common.PlayApp.system.oyunBus,
    db = oyun.db.Env.current)

}
