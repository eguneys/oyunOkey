import com.typesafe.sbt.packager.Keys.scriptClasspath
import com.typesafe.sbt.web.SbtWeb.autoImport._
import play.sbt.PlayImport._
import play.twirl.sbt.Import._
import PlayKeys._
import sbt._, Keys._

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
      scalaz, scalalib, hasher, typesafeConfig,
      reactivemongo.driver, reactivemongo.iteratees,
      okey,
      prismic,
      netty,
      kamon.core, java8compat, scaffeine),
    TwirlKeys.templateImports ++= Seq(
      "oyun.game.{ Game, Player, Pov }",
      "oyun.masa.Masa",
      "oyun.user.{ User, UserContext }",
      "oyun.api.Context",
      "oyun.app.templating.Environment._",
      "oyun.common.paginator.Paginator"
    ),
    // watchSources <<= sourceDirectory in Compile map { sources =>
    //   (sources ** "*").get
    // },
    // trump sbt-web into not looking at public/
    resourceDirectory in Assets := (sourceDirectory in Compile).value / "assets"))

lazy val modules = Seq(common, rating, db, user, chat, pref, security, game, setup, site, lobby, socket, hub, round, masa, i18n)

lazy val moduleRefs = modules map projectToRef
lazy val moduleCPDeps = moduleRefs map { new sbt.ClasspathDependency(_, None) }

lazy val api = module("api", moduleCPDeps)
  .settings(libraryDependencies ++= provided(
    play.api, reactivemongo.driver)
  ) aggregate (moduleRefs: _*)

lazy val user = module("user", Seq(common, rating, memo, hub, db)).settings(
  libraryDependencies ++= provided(play.api, play.test, reactivemongo.driver, hasher)
)

lazy val security = module("security", Seq(common, db, user)).settings(
  libraryDependencies ++= provided(play.api, play.test, reactivemongo.driver, ws)
)

lazy val game = module("game", Seq(common, db, user, chat)).settings(
  libraryDependencies ++= provided(play.api, play.test, reactivemongo.driver)
)

lazy val hub = module("hub", Seq(common)).settings(
  libraryDependencies ++= provided(play.api, play.test)
)

lazy val db = module("db", Seq(common)).settings(
  libraryDependencies ++= provided(play.api, play.test, reactivemongo.driver)
)

lazy val setup = module("setup", Seq(common, user, lobby, masa, hub)).settings(
  libraryDependencies ++= provided(play.api, play.test)
)

lazy val masa = module("masa", Seq(common, user, game, db, socket, hub, memo)).settings(
  libraryDependencies ++= provided(play.api, play.test, reactivemongo.driver)
)

lazy val fishnet = module("fishnet", Seq(common, game, db)).settings(
  libraryDependencies ++= provided(play.api, reactivemongo.driver)
)

lazy val round = module("round", Seq(common, user, chat, game, socket, hub, fishnet)).settings(
  libraryDependencies ++= provided(play.api, play.test, reactivemongo.driver)
)

lazy val lobby = module("lobby", Seq(common, user, game, socket, hub)).settings(
  libraryDependencies ++= provided(play.api, reactivemongo.driver)
)

lazy val i18n = module("i18n", Seq(common, user)).settings(
  libraryDependencies ++= provided(play.api, reactivemongo.driver)
)

lazy val memo = module("memo", Seq(common, db)).settings(
  libraryDependencies ++= Seq(spray.caching, scaffeine) ++ provided(play.api, play.test, reactivemongo.driver)
)

lazy val chat = module("chat", Seq(common, db, user, security, i18n)).settings(
  libraryDependencies ++= Seq(spray.caching) ++ provided(play.api, reactivemongo.driver)
)

lazy val pref = module("pref", Seq(common, db, user)).settings(
  libraryDependencies ++= provided(play.api, reactivemongo.driver)
)


lazy val site = module("site", Seq(common, socket)).settings(
  libraryDependencies ++= provided(play.api)
)

lazy val socket = module("socket", Seq(common, memo, hub)).settings(
  libraryDependencies ++= provided(play.api, play.test)
)

lazy val common = module("common").settings(
  libraryDependencies ++= provided(play.api, play.test, kamon.core)
)

lazy val rating = module("rating", Seq(common, db)).settings(
  libraryDependencies ++= provided(play.api, reactivemongo.driver)
)
