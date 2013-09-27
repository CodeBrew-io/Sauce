import sbt._

object Dependencies {
	val akkaVersion = "2.2.1"
	val akka = "com.typesafe.akka" %% "akka-actor" % akkaVersion
	val akkaRemote = "com.typesafe.akka" %% "akka-remote" % akkaVersion
	val akkaKernel = "com.typesafe.akka" %% "akka-kernel" % akkaVersion

	val akkaStack = Seq(
		akka,
		akkaRemote,
		akkaKernel
	)

	val webjars =  "org.webjars" %% "webjars-play" % "2.2.0-RC1"
	val bootstrap = "org.webjars" % "bootstrap" % "3.0.0"
	val jquery = "org.webjars" % "jquery" % "2.0.3"

	val frontEnd = Seq(
		webjars,
		bootstrap,
		jquery
	)
}