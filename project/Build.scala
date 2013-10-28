import sbt._
import Keys._
import play.Project._

import com.typesafe.sbt._
import SbtNativePackager._
import packager.Keys._

import com.jamesward.play.BrowserNotifierPlugin._

import com.github.mumoshu.play2.typescript.TypeScriptPlugin._

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

  lazy val scalaEval = Project(
    id = "scalaEval",
    base = file("scalaEval"),
    settings = Settings.default ++ packageArchetype.java_application ++ Seq(
      name := "scalaEval",
      resolvers := Seq("codebrew's maven" at "http://codebrew-io.github.io/maven/"),
      libraryDependencies += insight,
      bashScriptExtraDefines += """addJava "-Duser.dir=$(cd "${app_home}/.."; pwd -P)" """
    ) 
  ) dependsOn(evalApi, lookupApi)
 
  lazy val web = Project(
    id = "web",
    base = file("web"),
    settings = Settings.default ++ playScalaSettings ++ typescript ++ Seq(
      libraryDependencies ++= Seq(securesocial, scalastic) ++ frontEnd ++ test,
      resolvers += Resolver.url("sbt-plugin-snapshots", 
        new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns),
      tsOptions := Seq()
    ) ++ livereload
  ) dependsOn(evalApi, lookupApi)
}