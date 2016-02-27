import play.sbt.PlayImport._
import sbt._, Keys._

object Dependencies {
  object Resolvers {
    val typesafe = "typesafe.com" at "http://repo.typesafe.com/typesafe/releases/"

    val commons = Seq(
      typesafe
    )
  }


  val scalaz = "org.scalaz" %% "scalaz-core" % "7.1.7"
  val scalalib = "com.github.ornicar" %% "scalalib" % "5.4"
  val config = "com.typesafe" % "config" % "1.3.0"
  val RM = "org.reactivemongo" %% "reactivemongo" % "0.11.9"
  val PRM = "org.reactivemongo" %% "play2-reactivemongo" % "0.11.9"

  object play {
    val version = "2.4.6"
    val api = "com.typesafe.play" %% "play" % version
    val test = "com.typesafe.play" %% "play-test" % version
  }
}
