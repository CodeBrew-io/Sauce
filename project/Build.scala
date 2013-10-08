import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  import Dependencies._

  val evalApi = Project(
    id = "eval-api",
    base = file("eval-api"),
    settings = Settings.scrooge ++ Seq(
      name := "eval-api"
    )
  )

  val lookupApi = Project(
    id = "lookup-api",
    base = file("lookup-api"),
    settings = Settings.scrooge ++ Seq(
      name := "lookup-api"
    )
  )

  val eval = Project(
    id = "eval",
    base = file("eval"),
    settings = Project.defaultSettings ++ Settings.default ++ Settings.noplay ++ Seq(
      name := "eval",
      resolvers := Seq("gui maven" at "http://masseguillaume.github.io/maven"),
      libraryDependencies += insight
    )
  ).dependsOn(evalApi, lookupApi)

  val main = play.Project(
    "server", 
    Settings.appVersion, 
    frontEnd ++ test
  ).settings(Settings.default: _*).dependsOn(evalApi, lookupApi)
}