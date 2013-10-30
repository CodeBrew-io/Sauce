import sbt._
import Keys._

import com.typesafe.sbt.SbtGit._

object Settings {
	val default = Project.defaultSettings ++ Seq(
		scalaVersion := "2.10.3",
		organization := "ca.polymtl.log4900"
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
}