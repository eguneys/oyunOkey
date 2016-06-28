import com.typesafe.sbt.packager.Keys.scriptClasspath
import com.typesafe.sbt.web.SbtWeb.autoImport._
import play.sbt.PlayImport._
import play.twirl.sbt.Import._
import PlayKeys._
import sbt._, Keys._

object ApplicationBuild extends Build {
  import BuildSettings._
  import Dependencies._

  lazy val root = Project("oyun", file("."))
    .enablePlugins(_root_.play.sbt.PlayScala)
    .dependsOn(api)
    .aggregate(api)
    .settings(Seq(
      scalaVersion := globalScalaVersion,
      resolvers ++= Dependencies.Resolvers.commons,
      scalacOptions := compilerOptions,
      // disable publishing the main API jar
      publishArtifact in (Compile, packageDoc) := false,
      // disable publishing the main sources jar
      publishArtifact in (Compile, packageSrc) := false,
      // don't stage the conf dir
      externalizeResources := false,
      // shorter prod classpath
      scriptClasspath := Seq("*"),
      libraryDependencies ++= Seq(
        scalaz, scalalib, hasher, config, RM,
        spray.caching, prismic,
        kamon.core, java8compat),
      TwirlKeys.templateImports ++= Seq(
        "oyun.game.{ Game, Player, Pov }",
        "oyun.masa.Masa",
        "oyun.user.{ User, UserContext }",
        "oyun.api.Context",
        "oyun.app.templating.Environment._",
        "oyun.common.paginator.Paginator"
      ),
      watchSources <<= sourceDirectory in Compile map { sources =>
        (sources ** "*").get
      },
      // trump sbt-web into not looking at public/
      resourceDirectory in Assets := (sourceDirectory in Compile).value / "assets"))

  lazy val modules = Seq(common, rating, db, user, chat, pref, security, game, setup, site, lobby, socket, hub, okey, round, masa, i18n)

  lazy val moduleRefs = modules map projectToRef
  lazy val moduleCPDeps = moduleRefs map { new sbt.ClasspathDependency(_, None) }

  lazy val api = project("api", moduleCPDeps)
    .settings(libraryDependencies ++= provided(
      play.api, RM)
  ) aggregate (moduleRefs: _*)

  lazy val user = project("user", Seq(common, rating, memo, hub, db)).settings(
    libraryDependencies ++= provided(play.api, play.test, RM, hasher)
  )

  lazy val security = project("security", Seq(common, db, user)).settings(
    libraryDependencies ++= provided(play.api, play.test, RM, ws)
  )

  lazy val game = project("game", Seq(common, db, user, chat, okey)).settings(
    libraryDependencies ++= provided(play.api, play.test, RM)
  )

  lazy val hub = project("hub", Seq(common, okey)).settings(
    libraryDependencies ++= provided(play.api, play.test)
  )

  lazy val db = project("db", Seq(common)).settings(
    libraryDependencies ++= provided(play.api, play.test, RM)
  )

  lazy val setup = project("setup", Seq(common, user, lobby, masa, hub)).settings(
    libraryDependencies ++= provided(play.api, play.test)
  )

  lazy val masa = project("masa", Seq(common, user, game, db, socket, hub, memo)).settings(
    libraryDependencies ++= provided(play.api, play.test, RM)
  )

  lazy val fishnet = project("fishnet", Seq(common, okey, game, db)).settings(
    libraryDependencies ++= provided(play.api, RM)
  )

  lazy val round = project("round", Seq(common, user, chat, game, okey, socket, hub, fishnet)).settings(
    libraryDependencies ++= provided(play.api, play.test, RM)
  )

  lazy val lobby = project("lobby", Seq(common, user, game, okey, socket, hub)).settings(
    libraryDependencies ++= provided(play.api, RM)
  )

  lazy val i18n = project("i18n", Seq(common, user)).settings(
    libraryDependencies ++= provided(play.api, RM)
  )

  lazy val memo = project("memo", Seq(common, db)).settings(
    libraryDependencies ++= Seq(spray.caching) ++ provided(play.api, play.test, RM)
  )

  lazy val chat = project("chat", Seq(common, db, user, security, i18n)).settings(
    libraryDependencies ++= Seq(spray.caching) ++ provided(play.api, RM)
  )

  lazy val pref = project("pref", Seq(common, db, user)).settings(
    libraryDependencies ++= provided(play.api, RM)
  )


  lazy val site = project("site", Seq(common, socket)).settings(
    libraryDependencies ++= provided(play.api)
  )

  lazy val socket = project("socket", Seq(common, memo, hub)).settings(
    libraryDependencies ++= provided(play.api, play.test)
  )

  lazy val common = project("common").settings(
    libraryDependencies ++= provided(play.api, play.test, kamon.core)
  )

  lazy val rating = project("rating", Seq(common, db, okey)).settings(
    libraryDependencies ++= provided(play.api, RM)
  )

  lazy val okey = project("okey")
}
