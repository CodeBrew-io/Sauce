import sbt._
import Keys._

import com.typesafe.sbt._
import SbtGit._
import SbtNativePackager._
import packager.Keys._

object Settings {
	lazy val default = Project.defaultSettings ++ Seq(
		scalaVersion := "2.10.3",
		organization := "io.codebrew"
	) ++ versionWithGit

	// scrooge
	import com.twitter.scrooge._
	import ScroogeSBT._
	lazy val scrooge = 
		Settings.default ++ 
		ScroogeSBT.newSettings ++ 
		Seq(
    		scroogeBuildOptions := Seq("--ostrich","--finagle"),
    		libraryDependencies ++= Dependencies.scroogeStack
    	)

    val setupReplClassPath = TaskKey[Unit]("setup-repl-classpath", "Set up the repl server's classpath based on our dependencies.")

    lazy val repl = Seq(
		Settings.setupReplClassPath <<= (dependencyClasspath in Compile) map {cp =>
			val cpStr = cp map { case Attributed(str) => str} mkString(System.getProperty("path.separator"))
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