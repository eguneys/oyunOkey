import sbt._, Keys._

object BuildSettings {
  import Dependencies._

  val globalScalaVersion = "2.11.7"

  def buildSettings = Defaults.defaultSettings ++ Seq(
    scalaVersion := globalScalaVersion,
    resolvers ++= Dependencies.Resolvers.commons
  )

  def defaultDeps = Seq(scalaz, scalalib)

  def provided(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")

  def project(name: String, deps: Seq[sbt.ClasspathDep[sbt.ProjectReference]] = Seq.empty) =
    Project(
      name,
      file("modules/" + name),
      dependencies = deps,
      settings = Seq(
        version:= "2.0",
        libraryDependencies := defaultDeps
      ) ++ buildSettings ++ srcMain
    )

  val srcMain = Seq(
    scalaSource in Compile <<= (sourceDirectory in Compile)(identity),
    scalaSource in Test <<= (sourceDirectory in Test)(identity)
  )

  def projectToRef(p: Project): ProjectReference = LocalProject(p.id)
}
