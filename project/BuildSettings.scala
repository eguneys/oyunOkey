import sbt._, Keys._

object BuildSettings {
  import Dependencies._

  val globalScalaVersion = "2.11.12"

  def buildSettings = Defaults.coreDefaultSettings ++ Seq(
    scalaVersion := globalScalaVersion,
    resolvers ++= Dependencies.Resolvers.commons,
    scalacOptions := compilerOptions,
    incOptions := incOptions.value.withNameHashing(true),
    updateOptions := updateOptions.value.withCachedResolution(true),
    sources in doc in Compile := List(),
    // disable publishing the main API jar
    publishArtifact in (Compile, packageDoc) := false,
    // disable publishing the main sources jar
    publishArtifact in (Compile, packageSrc) := false
  )

  def defaultDeps = Seq(scalaz, okey, scalalib)

  def provided(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")

  def module(name: String, deps: Seq[sbt.ClasspathDep[sbt.ProjectReference]] = Seq.empty) =
    Project(
      name,
      file("modules/" + name),
      dependencies = deps,
      settings = Seq(
        version:= "2.0",
        libraryDependencies := defaultDeps
      ) ++ buildSettings ++ srcMain
    )

  val compilerOptions = Seq(
    "-deprecation", "-unchecked", "-feature", "-language:_",
    "-Xfatal-warnings",
    "-Ywarn-dead-code",
    "-Ybackend:GenBCode",
    "-Ydelambdafy:method", "-target:jvm-1.8")

  val srcMain = Seq(
    scalaSource in Compile := (sourceDirectory in Compile).value,
    scalaSource in Test := (sourceDirectory in Test).value
  )

  def projectToRef(p: Project): ProjectReference = LocalProject(p.id)
}
