import sbt._
import Keys._
import play.Project._

import bintray.Plugin.bintrayResolverSettings

import Settings._

object ApplicationBuild extends Build {

  import Dependencies._

  lazy val api = Project(
    id = "api",
    base = file("api"),
    settings = scrooge ++ service ++ Seq(
      name := "api",
      exportJars := true
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
        "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
      ),
      libraryDependencies ++= Seq(insight, specs2)
    ) 
  ) dependsOn(api)
 
  lazy val web = Project(
    id = "web",
    base = file("web"),
    settings = default ++ playScalaSettings ++ Seq(
      libraryDependencies ++= Seq(securesocial, scalastic, jdbc, anorm, specs2) ++ frontEnd,
      resolvers += Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns)
    )
  ) dependsOn(api)
}