import sbt._
import Keys._

import com.typesafe.sbt._
import SbtGit._
import SbtNativePackager._
import packager.Keys._

object Settings {
	lazy val scalaEvalVersionMM = "2.11"
	lazy val scalaEvalVersion = s"${scalaEvalVersionMM}.0"

	lazy val scalaWebVersionMM = "2.10"
	lazy val scalaWebVersion = s"${scalaWebVersionMM}.3"

	lazy val default = Project.defaultSettings ++ Seq(
		organization := "io.codebrew",
		resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"
	) ++ versionWithGit

    val setupReplClassPath = TaskKey[Unit]("setup-repl-classpath", "Set up the repl server's classpath based on our dependencies.")

    lazy val repl = Seq(
		Settings.setupReplClassPath <<= (dependencyClasspath in Compile, classDirectory in Compile) map {(cp, source) =>
			val deps = cp.map { case Attributed(str) => str}
			val cpTot = source +: deps
			val cpStr = cpTot.mkString(System.getProperty("path.separator"))
			System.setProperty("replhtml.class.path", cpStr)
		},
		run in Compile <<= (run in Compile).dependsOn(Settings.setupReplClassPath),
		console in Compile <<= (console in Compile).dependsOn(Settings.setupReplClassPath),
		test in Test <<= (test in Test).dependsOn(Settings.setupReplClassPath),
		testOnly in Test <<= (testOnly in Test).dependsOn(Settings.setupReplClassPath),
	    bashScriptExtraDefines += {
			def relativeClassPath(cp: Seq[String]): String = {
				cp map (n => "$lib_dir/"+n) mkString ":"
			}
			s"""addJava "-Dreplhtml.class.path=${relativeClassPath(scriptClasspath.value)}" """
		}
	)

	lazy val setHome = bashScriptExtraDefines += """addJava "-Duser.dir=$(cd "${app_home}/.."; pwd -P)" """

    lazy val service = packageArchetype.java_application ++ Seq(
    	setHome
    )
}