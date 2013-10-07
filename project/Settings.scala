import sbt._
import Keys._

object Settings {
	val appVersion = "0.1.0-SNAPSHOT"
	val default = Seq(
		scalaVersion := "2.10.3",
		organization := "ca.polymtl.log4900",
		version := appVersion
	)

	// no play
	import play.Keys._
	val noplay = Seq(
		playAssetsDirectories := Seq() // no livereload
	)

	// thrift
	import com.twitter.scrooge._
	import ScroogeSBT._
	lazy val scrooge = 
		Project.defaultSettings ++ 
		Settings.default ++ 
		ScroogeSBT.newSettings ++ 
		noplay ++
		Seq(
    		scroogeBuildOptions := Seq("--ostrich","--finagle"),
    		libraryDependencies ++= Dependencies.scroogeStack
    	)
}