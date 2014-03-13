import sbt._
import Keys._
import play.Project._

import bintray.Plugin.bintrayResolverSettings
import com.github.bigtoast.sbtthrift.ThriftPlugin

import Settings._

object ApplicationBuild extends Build {

  import Dependencies._
  import Resolvers._

  // crossbuild give obscure errors, so we duplicate code
  lazy val apiSettings = default ++ service ++ ThriftPlugin.thriftSettings ++ Seq(
    name := "api",
    exportJars := true,
    libraryDependencies ++= thrift
  )

  lazy val api1 = Project(
    id = "api1",
    base = file("api1"),
    settings = apiSettings ++ Seq(
      scalaVersion := scalaEvalVersion
    )
  )

  lazy val api2 = Project(
    id = "api2",
    base = file("api2"),
    settings = apiSettings ++ Seq(
      scalaVersion := scalaWebVersion
    )
  )

  lazy val scalaEval = Project(
    id = "scalaEval",
    base = file("scalaEval"),
    settings = default ++ service ++ bintrayResolverSettings ++ repl ++ Seq(
      name := "scalaEval",
      scalaVersion := scalaEvalVersion,
      resolvers ++= Seq(bintray_masseguillaume, sonatype_snapshots),
      libraryDependencies ++= Seq(insight(scalaEvalVersion), specs2(scalaEvalVersion)),
      initialCommands in console := ""
    ) 
  ) dependsOn(api1)
 
  lazy val web = Project(
    id = "web",
    base = file("web"),
    settings = default ++ playScalaSettings ++ Seq(
      scalaVersion := scalaWebVersion,
      libraryDependencies ++= Seq(securesocial, elastic4s, jdbc, anorm, specs2s(scalaWebVersionMM)) ++ frontEnd,
      resolvers ++= Seq(sbt_plugins_snapshots, sonatype_snapshots)
    )
  ) dependsOn(api2)
}