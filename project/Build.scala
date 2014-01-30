import sbt._
import Keys._
import play.Project._

import bintray.Plugin.bintrayResolverSettings

import Settings._

object ApplicationBuild extends Build {

  import Dependencies._

  lazy val api1 = Project(
    id = "api1",
    base = file("api1"),
    settings = scrooge ++ service ++ Seq(
      scalaVersion := "2.10.3",
      name := "api",
      exportJars := true
    )
  )

  lazy val api2 = Project(
    id = "api2",
    base = file("api2"),
    settings = scrooge ++ service ++ Seq(
      scalaVersion := "2.10.4-20131126-231426-da7395016c",
      name := "api",
      exportJars := true,
      resolvers ++= Seq(
        bintray.Opts.resolver.repo("masseguillaume", "maven"),
        bintray.Opts.resolver.repo("jedesah", "maven")
      )
    )
  )

  lazy val scalaEval = Project(
    id = "scalaEval",
    base = file("scalaEval"),
    settings = default ++ service ++ bintrayResolverSettings ++ repl ++ Seq(
      scalaVersion := "2.10.4-20131126-231426-da7395016c",
      name := "scalaEval",
      resolvers ++= Seq(
        bintray.Opts.resolver.repo("masseguillaume", "maven"),
        bintray.Opts.resolver.repo("jedesah", "maven"),
        "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
        "Concurrent Maven Repo" at "http://conjars.org/repo",
        "Clo" at "http://clojars.org/repo",
        "Typesafe" at "http://repo.typesafe.com/typesafe/repo"
      ),
      libraryDependencies ++= Seq(insight, specs2) ++ extra,
      initialCommands in console := ""
    ) 
  ) dependsOn(api2)
 
  lazy val web = Project(
    id = "web",
    base = file("web"),
    settings = default ++ playScalaSettings ++ Seq(
      libraryDependencies ++= Seq(securesocial, scalastic, jdbc, anorm, specs2) ++ frontEnd,
      resolvers += Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns)
    )
  ) dependsOn(api1)
}