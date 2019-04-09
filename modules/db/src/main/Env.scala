package oyun.db

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.{ Await }
import scala.util.{ Success, Failure }


import com.typesafe.config.Config
import reactivemongo.api._
import dsl._

import oyun.common.Chronometer

final class Env(
  name: String,
  config: Config,
  lifecycle: play.api.inject.ApplicationLifecycle) {

  private lazy val (connection, dbName) = {

    val driver = MongoDriver(config)

    registerDriverShutdownHook(driver)

    (for{
      parsedUri <- MongoConnection.parseURI(config getString "uri")
      con <- driver.connection(parsedUri, true)
      db <- parsedUri.db match {
        case Some(name) => Success(name)
        case _ => Failure[String](new IllegalArgumentException(s"cannot resolve connection from URI: $parsedUri"))
      }
    } yield con -> db).get
    
  }

  private lazy val lnm = s"$name ${connection.supervisor}/${connection.name}"

  private lazy val db =
    Chronometer.syncEffect(Await.result(connection database dbName, 5.seconds)) { lap =>
      logger.info(s"$lnm MongoDB connected in ${lap.showDuration}")
    }


  // lazy val db = {
  //   val parsedUri: MongoConnection.ParsedURI =
  //     MongoConnection.parseURI(config.getString("uri")) match {
  //       case Success(parsedUri) => parsedUri
  //       case Failure(e) => sys error s"Invalid mongodb.uri"
  //     }

  //   val connection = driver.connection(parsedUri)

  //   parsedUri.db.fold[DefaultDB](sys error s"cannot resolve database from URI: $parsedUri") { dbUri =>
  //     val db = DB(dbUri, connection)

  //     logger.info(s"""ReactiveMongoApi successfully started with DB '$dbUri'! Servers:\n\t\t${parsedUri.hosts.map { s => s"[${s._1}:${s._1}]" }.mkString("\n\t\t")}""")
  //     db
  //   }
  // }

  def apply(name: String): Coll = db(name)

  private def registerDriverShutdownHook(mongoDriver: MongoDriver): Unit =
    lifecycle.addStopHook { () => Future(mongoDriver.close()) }
}

object Env {
  lazy val current = new Env(
    name = "main",
    config = oyun.common.PlayApp loadConfig "mongodb",
    lifecycle = oyun.common.PlayApp.lifecycle
  )
}
