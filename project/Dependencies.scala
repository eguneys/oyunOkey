import play.sbt.PlayImport._
import sbt._, Keys._

object Dependencies {
  object Resolvers {
    val typesafe = "typesafe.com" at "http://repo.typesafe.com/typesafe/releases/"

    val sprayRepo = "spray repo" at "http://repo.spray.io"
    val prismic = "Prismic.io kits" at "https://s3.amazonaws.com/prismic-maven-kits/repository/maven/"
    val lilaMaven = "lila-maven" at "https://raw.githubusercontent.com/ornicar/lila-maven/master"

    val commons = Seq(
      typesafe,
      prismic,
      lilaMaven,
      sprayRepo
    )
  }


  val scalaz = "org.scalaz" %% "scalaz-core" % "7.2.16"
  val scalalib = "com.github.ornicar" %% "scalalib" % "6.6"
  val okey = "net.oyunkeyf" %% "scalaokey" % "1.0.0"
  val typesafeConfig = "com.typesafe" % "config" % "1.3.1"
  val hasher = "com.roundeights" %% "hasher" % "1.2.0"
  val prismic = "io.prismic" %% "scala-kit" % "1.3.7"
  val java8compat = "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0"
  val scaffeine = "com.github.blemale" %% "scaffeine" % "2.5.0" % "compile"
  val netty = "io.netty" % "netty" % "3.10.6.Final"

  object reactivemongo {
    val version = "0.12.4"
    val driver = ("org.reactivemongo" %% "reactivemongo" % version)
      .exclude("com.typesafe.akka", "*")
      .exclude("com.typesafe.play", "*")
    val iteratees = ("org.reactivemongo" %% "reactivemongo-iteratees" % version)
      .exclude("com.typesafe.akka", "*")
      .exclude("com.typesafe.play", "*")
  }
  
  object play {
    val version = "2.4.11"
    val api = "com.typesafe.play" %% "play" % version
    val test = "com.typesafe.play" %% "play-test" % version
  }

  object akka {
    val version = "2.4.20"
    val actor = "com.typesafe.akka" %% "akka-actor" % version
    val slf4j = "com.typesafe.akka" %% "akka-slf4j" % version
  }

  object spray {
    val version = "1.3.3"
    val caching = "io.spray" %% "spray-caching" % version
    val util = "io.spray" %% "spray-util" % version
  }

  object kamon {
    val version = "0.5.2"
    val core = "io.kamon" %% "kamon-core" % version
    val statsd = "io.kamon" %% "kamon-statsd" % version
  }
}
