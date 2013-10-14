import sbt._

object Dependencies {
	val insight = "com.github.jedesah" %% "codesheet-api" % "0.3-SNAPSHOT"

	private val finagleVer = "6.5.0"
	lazy val thrift = "org.apache.thrift" % "libthrift" % "0.8.0"
	val finagleCore = "com.twitter" %% "finagle-core" % finagleVer
	val finagleThrift = "com.twitter"  %% "finagle-thrift" % finagleVer
	//val finableOstrich = "com.twitter" %% "finagle-ostrich4" % finagleVer
	val scroogeRuntime = "com.twitter" %% "scrooge-runtime" % "3.9.0"

	val scroogeStack = Seq(  
		thrift,
		finagleCore, 
		finagleThrift,
		scroogeRuntime
	)

	val frontEnd = Seq(
		"org.webjars" % "angularjs" % "1.2.0-rc.2",
		"org.webjars" % "bootstrap" % "3.0.0",
		"org.webjars" % "codemirror" % "3.16",
		"org.webjars" % "font-awesome" % "3.2.1",
		"org.webjars" % "jquery" % "2.0.3",
		"org.webjars" %% "webjars-play" % "2.2.0-RC1"
	)

	val specs2 = "org.specs2" %% "specs2" % "2.2.2" % "test"
	val test = Seq(specs2)
	val securesocial = ("securesocial" %% "securesocial" % "master-SNAPSHOT") exclude("org.scala-stm", "scala-stm_2.10.0")
}