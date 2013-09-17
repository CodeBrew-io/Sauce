import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  import Dependencies._

  val eval = Project(
  	id = "eval", 
  	base = file("modules/eval"),
  	settings = Settings.default ++ Project.defaultSettings ++ Seq(
      libraryDependencies ++= akkaStack
  	)
  )

  val main = play.Project( "ws", 
    Settings.appVersion, 
    frontEnd
  ).settings(Settings.default: _*).
    dependsOn(eval)
}
