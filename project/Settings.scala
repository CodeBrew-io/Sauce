import sbt._
import Keys._

object Settings {
	val appVersion = "0.1.0-SNAPSHOT"
	val default = Seq(
		scalaVersion := "2.10.2",
		organization := "ca.polymtl.log4900",
		version := appVersion
	)
}