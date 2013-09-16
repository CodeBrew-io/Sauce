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
}