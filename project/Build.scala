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
      scalaz, scalalib),
    TwirlKeys.templateImports ++= Seq(
      "oyun.api.Context",
      "oyun.app.templating.Environment._"
    )
  ) dependsOn api aggregate api

  lazy val modules = Seq(common, user, setup, lobby, socket)

  lazy val moduleRefs = modules map projectToRef
  lazy val moduleCPDeps = moduleRefs map { new sbt.ClasspathDependency(_, None) }

  lazy val api = project("api", moduleCPDeps)
    .settings(libraryDependencies ++= provided(
      play.api)
  ) aggregate (moduleRefs: _*)

  lazy val user = project("user", Seq(common)).settings(
    libraryDependencies ++= provided(play.api, play.test)
  )

  lazy val setup = project("setup", Seq(common, user)).settings(
    libraryDependencies ++= provided(play.api, play.test)
  )

  lazy val lobby = project("lobby", Seq(common, user, socket)).settings(
    libraryDependencies ++= provided(play.api, play.test)
  )

  lazy val socket = project("socket", Seq(common)).settings(
    libraryDependencies ++= provided(play.api, play.test)
  )

  lazy val common = project("common").settings(
    libraryDependencies ++= provided(play.api, play.test)
  )
}
