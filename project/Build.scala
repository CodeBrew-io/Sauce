import sbt._
import Keys._
import play.Project._

import com.typesafe.sbt.SbtNativePackager.packageArchetype

object ApplicationBuild extends Build {

  import Dependencies._

  val evalApi = Project(
    id = "eval-api",
    base = file("eval-api"),
    settings = Settings.scrooge ++ Seq(
      name := "eval-api",
      exportJars := true
    )
  )

  val lookupApi = Project(
    id = "lookup-api",
    base = file("lookup-api"),
    settings = Settings.scrooge ++ Seq(
      name := "lookup-api",
      exportJars := true
    )
  )

  val scalaEval = Project(
    id = "scalaEval",
    base = file("scalaEval"),
    settings = Project.defaultSettings ++ Settings.default ++ Settings.noplay ++ Seq(
      name := "scalaEval",
      resolvers := Seq("gui maven" at "http://masseguillaume.github.io/maven"),
      libraryDependencies += insight
    ) ++ packageArchetype.java_application
  ).dependsOn(evalApi, lookupApi)
 
  val main = play.Project(
    "server", 
    "",
    frontEnd ++ test
  ).settings((Settings.default ++ Seq(
    libraryDependencies += securesocial,
    resolvers += Resolver.url("sbt-plugin-snapshots", 
      new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns))
  ): _*).
  dependsOn(evalApi, lookupApi)
}