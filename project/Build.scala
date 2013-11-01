import sbt._
import Keys._
import play.Project._

import com.typesafe.sbt._
import SbtNativePackager._
import packager.Keys._

import com.jamesward.play.BrowserNotifierPlugin._

import com.github.mumoshu.play2.typescript.TypeScriptPlugin._

import bintray.Plugin.bintrayResolverSettings

object ApplicationBuild extends Build {

  import Dependencies._

  lazy val evalApi = Project(
    id = "eval-api",
    base = file("eval-api"),
    settings = Settings.scrooge ++ Seq(
      name := "eval-api",
      exportJars := true
    )
  )

  lazy val lookupApi = Project(
    id = "lookup-api",
    base = file("lookup-api"),
    settings = Settings.scrooge ++ Seq(
      name := "lookup-api",
      exportJars := true
    )
  )

  val setupReplClassPath = TaskKey[Unit]("setup-repl-classpath", "Set up the repl server's classpath based on our dependencies.")

  lazy val scalaEval = Project(
    id = "scalaEval",
    base = file("scalaEval"),
    settings = Settings.default ++ packageArchetype.java_application ++ bintrayResolverSettings ++ Seq(
      name := "scalaEval",
      resolvers += bintray.Opts.resolver.repo("jedesah", "maven"),
      libraryDependencies ++= Seq(insight) ++ test,
      bashScriptExtraDefines += {
        val cpStr = (dependencyClasspath in Compile).value map { case Attributed(str) => str} mkString(System.getProperty("path.separator"))
        """addJava "-Dreplhtml.class.path=$cpStr" """
      },
      bashScriptExtraDefines += """addJava "-Duser.dir=$(cd "${app_home}/.."; pwd -P)" """,
      setupReplClassPath <<= (dependencyClasspath in Compile) map {cp =>
        val cpStr = cp map { case Attributed(str) => str} mkString(System.getProperty("path.separator"))
        println("Repl will use classpath "+ cpStr)
        System.setProperty("replhtml.class.path", cpStr)
      },
      run in Compile <<= (run in Compile).dependsOn(setupReplClassPath)
    ) 
  ) dependsOn(evalApi, lookupApi)
 
  lazy val web = Project(
    id = "web",
    base = file("web"),
    settings = Settings.default ++ playScalaSettings ++ typescript ++ Seq(
      libraryDependencies ++= Seq(securesocial, scalastic, jdbc, anorm) ++ frontEnd ++ test,
      resolvers += Resolver.url("sbt-plugin-snapshots", 
        new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns),
      tsOptions := Seq()
    ) ++ livereload
  ) dependsOn(evalApi, lookupApi)
}