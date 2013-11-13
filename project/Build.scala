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

  lazy val eval = Project(
    id = "eval",
    base = file("eval"),
    settings = default ++ service ++ bintrayResolverSettings ++ repl ++ Seq(
      name := "eval",
      resolvers += bintray.Opts.resolver.repo("jedesah", "maven"),
      libraryDependencies ++= Seq(insight, specs2)
    ) 
  ) dependsOn(api)
 
  lazy val web = Project(
    id = "web",
    base = file("web"),
    settings = default ++ playScalaSettings ++ Seq(
      libraryDependencies ++= Seq(securesocial, scalastic, jdbc, anorm, specs2) ++ frontEnd,
      resolvers += Resolver.url("sbt-plugin-snapshots", 
        new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns)
    )
  ) dependsOn(api)
}