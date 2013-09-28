import sbt._

object Dependencies {
	val akkaVersion = "2.2.1"
	val akka = "com.typesafe.akka" %% "akka-actor" % akkaVersion

	private val finagleVer = "6.5.0"
	val finagleCore = "com.twitter" %% "finagle-core" % finagleVer
	val finagleThrift = "com.twitter"  %% "finagle-thrift" % finagleVer
	val finableOstrich = "com.twitter" %% "finagle-ostrich4" % finagleVer
	val scroogeRuntime = "com.twitter" %% "scrooge-runtime" % "3.9.0"

	val scroogeStack = Seq(  
		finagleCore, 
		finagleThrift,
		scroogeRuntime
	)

	val frontEnd = Seq(
		"org.webjars" %% "webjars-play" % "2.2.0-RC1",
		"org.webjars" % "bootstrap" % "3.0.0",
		"org.webjars" % "jquery" % "2.0.3"
	)

	val specs2 = "org.specs2" %% "specs2" % "2.2.2" % "test"
	val test = Seq(specs2)
}