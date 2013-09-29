import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  import Dependencies._

  val api = Project(
    id = "api",
    base = file("api"),
    settings = Project.defaultSettings ++ Settings.default ++ Settings.scrooge ++ Seq(
      name := "api",
      playAssetsDirectories := Seq() // no livereload
    )
  )

  val eval = Project(
    id = "eval",
    base = file("eval"),
    settings = Project.defaultSettings ++ Settings.default ++ Seq(
      name := "eval",
      resolvers := Seq("gui maven" at "http://masseguillaume.github.io/maven"),
      libraryDependencies ++= Seq(finableOstrich, insight),
      playAssetsDirectories := Seq() // no livereload
    )
  ) dependsOn( api )

  val main = play.Project(
    "server", 
    Settings.appVersion, 
    frontEnd ++ test
  ).settings(Settings.default: _*).dependsOn(api)
}