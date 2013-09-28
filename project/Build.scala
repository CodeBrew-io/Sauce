import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  import Dependencies._

  val api = Project(
    id = "api",
    base = file("modules/api"),
    settings = Project.defaultSettings ++ Settings.default ++ Settings.scrooge ++ Seq(
      name := "api",
      playAssetsDirectories := Seq()
    )
  )

  val eval = Project(
    id = "eval",
    base = file("modules/eval"),
    settings = Project.defaultSettings ++ Settings.default ++ Seq(
      name := "eval",
      libraryDependencies += finableOstrich,
      playAssetsDirectories := Seq()
    )
  ) dependsOn( api )

  val main = play.Project(
    "server", 
    Settings.appVersion, 
    frontEnd ++ test
  ).settings(Settings.default: _*).
    dependsOn(api)
}