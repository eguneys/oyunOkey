import play.sbt.PlayImport._
import play.twirl.sbt.Import._
import sbt._, Keys._

object ApplicationBuild extends Build {
  import BuildSettings._
  import Dependencies._

  lazy val root = Project("oyun", file(".")) enablePlugins _root_.play.sbt.PlayScala settings (
    scalaVersion := globalScalaVersion,
    resolvers ++= Dependencies.Resolvers.commons,
    libraryDependencies ++= Seq(
      scalaz, scalalib, RM, PRM),
    TwirlKeys.templateImports ++= Seq(
      "oyun.game.{ Game, Player, Pov }",
      "oyun.masa.Masa",
      "oyun.api.Context",
      "oyun.app.templating.Environment._"
    )
  ) dependsOn api aggregate api

  lazy val modules = Seq(common, db, user, game, setup, lobby, socket, hub, okey, round, masa)

  lazy val moduleRefs = modules map projectToRef
  lazy val moduleCPDeps = moduleRefs map { new sbt.ClasspathDependency(_, None) }

  lazy val api = project("api", moduleCPDeps)
    .settings(libraryDependencies ++= provided(
      play.api, RM)
  ) aggregate (moduleRefs: _*)

  lazy val user = project("user", Seq(common, memo)).settings(
    libraryDependencies ++= provided(play.api, play.test)
  )

  lazy val game = project("game", Seq(common, db, okey)).settings(
    libraryDependencies ++= provided(play.api, play.test, RM, PRM)
  )

  lazy val hub = project("hub", Seq(common, okey)).settings(
    libraryDependencies ++= provided(play.api, play.test)
  )

  lazy val db = project("db", Seq(common)).settings(
    libraryDependencies ++= provided(play.api, play.test, RM, PRM)
  )

  lazy val setup = project("setup", Seq(common, user, lobby, masa, hub)).settings(
    libraryDependencies ++= provided(play.api, play.test)
  )

  lazy val masa = project("masa", Seq(common, user, game, db, socket, hub, memo)).settings(
    libraryDependencies ++= provided(play.api, play.test, RM, PRM)
  )

  lazy val round = project("round", Seq(common, user, game, okey, socket, hub)).settings(
    libraryDependencies ++= provided(play.api, play.test, RM, PRM)
  )

  lazy val lobby = project("lobby", Seq(common, user, game, okey, socket, hub)).settings(
    libraryDependencies ++= provided(play.api, play.test)
  )

  lazy val memo = project("memo").settings(
    libraryDependencies ++= provided(play.api, play.test)
  )

  lazy val socket = project("socket", Seq(common, hub)).settings(
    libraryDependencies ++= provided(play.api, play.test)
  )

  lazy val common = project("common").settings(
    libraryDependencies ++= provided(play.api, play.test)
  )

  lazy val okey = project("okey")
}
