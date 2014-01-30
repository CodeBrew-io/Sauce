import sbt._

object Dependencies {
	val insight = "com.github.jedesah" %% "scalainsight" % "1.0.0"

	private val finagleVer = "6.5.0"
	lazy val thrift = "org.apache.thrift" % "libthrift" % "0.8.0"
	val finagleCore = "com.twitter" % "finagle-core_2.10" % finagleVer
	val finagleThrift = "com.twitter"  % "finagle-thrift_2.10" % finagleVer
	//val finableOstrich = "com.twitter" % "finagle-ostrich4_2.10" % finagleVer
	val scroogeRuntime = "com.twitter" % "scrooge-runtime_2.10" % "3.9.0"

	val scroogeStack = Seq(  
		thrift,
		finagleCore, 
		finagleThrift,
		scroogeRuntime
	)

	val frontEnd = Seq(
		"org.webjars" % "bootstrap" % "3.0.0",
		"org.webjars" % "font-awesome" % "4.0.3",
		"org.webjars" % "angularjs" % "1.2.2",
		"org.webjars" %% "webjars-play" % "2.2.0-RC1"
	)

	val specs2 = "org.specs2" % "specs2_2.10" % "2.2.3" % "test"
	val scalastic = "org.scalastic" %% "scalastic" % "0.90.2"
	val securesocial = ("securesocial" %% "securesocial" % "master-SNAPSHOT") exclude("org.scala-stm", "scala-stm_2.10.0")

	// extra deps for fun
	val extra = Seq(
		"org.apache.hadoop" % "hadoop-core" % "1.2.1",
		"org.scalaz" % "scalaz-core_2.10" % "7.1.0-M4",
		"org.scalaz" % "scalaz-concurrent_2.10" % "7.1.0-M4",
		"org.scalaz" % "scalaz-effect_2.10" % "7.1.0-M4",
		"org.scalaz" % "scalaz-iteratee_2.10" % "7.1.0-M4",
		"org.scalaz" % "scalaz-typelevel_2.10" % "7.1.0-M4",
		"org.scalaz" % "scalaz-xml_2.10" % "7.1.0-M4",
		"org.scalaz" % "scalaz-scalacheck-binding_2.10" % "7.1.0-M4",
		"org.spire-math" % "spire_2.10" % "0.7.1",
		"com.twitter" % "scalding-core_2.10" % "0.9.0rc4",
		"com.twitter" % "scalding-args_2.10" % "0.9.0rc4",
		"com.twitter" % "scalding-date_2.10" % "0.9.0rc4",
		"com.twitter" % "scalding-commons_2.10" % "0.9.0rc4",
		"com.twitter" % "scalding-avro_2.10" % "0.9.0rc4",
		"com.twitter" % "bijection-core_2.10" % "0.6.0",
		"com.twitter" % "bijection-protobuf_2.10" % "0.6.0",
		"com.twitter" % "bijection-thrift_2.10" % "0.6.0",
		"com.twitter" % "bijection-guava_2.10" % "0.6.0",
		"com.twitter" % "bijection-scrooge_2.10" % "0.6.0",
		"com.twitter" % "bijection-json_2.10" % "0.6.0",
		"com.twitter" % "bijection-util_2.10" % "0.6.0",
		"com.twitter" % "bijection-clojure_2.10" % "0.6.0",
		"com.twitter" % "bijection-netty_2.10" % "0.6.0",
		"com.twitter" % "bijection-avro_2.10" % "0.6.0",
		"com.twitter" % "bijection-hbase_2.10" % "0.6.0",
		"com.twitter" % "algebird-core_2.10" % "0.3.0",
		"com.twitter" % "algebird-test_2.10" % "0.3.0",
		"com.twitter" % "algebird-util_2.10" % "0.3.0",
		"com.twitter" % "algebird-bijection_2.10" % "0.3.0"
	)
}