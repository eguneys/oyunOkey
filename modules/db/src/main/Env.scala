package oyun.db

import scala.concurrent.Future
import scala.util.{ Success, Failure }

import com.typesafe.config.Config
import reactivemongo.api._
import Types._

final class Env(
  config: Config,
  lifecycle: play.api.inject.ApplicationLifecycle) {


  lazy val db = {
    val parsedUri: MongoConnection.ParsedURI =
      MongoConnection.parseURI(config.getString("uri")) match {
        case Success(parsedUri) => parsedUri
        case Failure(e) => sys error s"Invalid mongodb.uri"
      }

    val driver = new MongoDriver(Some(config))
    val connection = driver.connection(parsedUri)

    parsedUri.db.fold[DefaultDB](sys error s"cannot resolve database from URI: $parsedUri") { dbUri =>
      val db = DB(dbUri, connection)
      registerDriverShutdownHook(driver)
      logger.info(s"""ReactiveMongoApi successfully started with DB '$dbUri'! Servers:\n\t\t${parsedUri.hosts.map { s => s"[${s._1}:${s._1}]" }.mkString("\n\t\t")}""")
      db
    }
  }

  def apply(name: String): Coll = db(name)

  private def registerDriverShutdownHook(mongoDriver: MongoDriver): Unit =
    lifecycle.addStopHook { () => Future(mongoDriver.close()) }
}

object Env {
  lazy val current = new Env(
    config = oyun.common.PlayApp loadConfig "mongodb",
    lifecycle = oyun.common.PlayApp.lifecycle
  )
}
