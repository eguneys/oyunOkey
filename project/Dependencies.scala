import play.sbt.PlayImport._
import sbt._, Keys._

object Dependencies {
  object Resolvers {
    val typesafe = "typesafe.com" at "http://repo.typesafe.com/typesafe/releases/"

    val sprayRepo = "spray repo" at "http://repo.spray.io"
    val prismic = "Prismic.io kits" at "https://s3.amazonaws.com/prismic-maven-kits/repository/maven/"

    val commons = Seq(
      typesafe,
      prismic,
      sprayRepo
    )
  }


  val scalaz = "org.scalaz" %% "scalaz-core" % "7.1.7"
  val scalalib = "com.github.ornicar" %% "scalalib" % "5.4"
  val config = "com.typesafe" % "config" % "1.3.0"
  val RM = "org.reactivemongo" %% "reactivemongo" % "0.11.10"
  val prismic = "io.prismic" %% "scala-kit" % "1.3.7"
  val java8compat = "org.scala-lang.modules" %% "scala-java8-compat" % "0.7.0"

  object play {
    val version = "2.4.6"
    val api = "com.typesafe.play" %% "play" % version
    val test = "com.typesafe.play" %% "play-test" % version
  }

  object spray {
    val version = "1.3.3"
    val caching = "io.spray" %% "spray-caching" % version
    val util = "io.spray" %% "spray-util" % version
  }
}
