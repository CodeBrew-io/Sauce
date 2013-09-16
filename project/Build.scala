import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  import Dependencies._

  val appName         = "ws"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq()

  val main = play.Project(appName, appVersion, appDependencies)
}
