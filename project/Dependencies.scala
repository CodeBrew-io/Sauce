import sbt._

object Dependencies {
	def insight(v: String) = "io.codebrew" % s"simple-insight_$v" % "0.1.0"
	def specs2s(v: String) = "org.specs2" % s"specs2_$v" % "2.4-SNAPSHOT" % "test"
	def specs2(v: String) = "org.specs2" % s"specs2_$v" % "2.3.10" % "test"
	

	val frontEnd = Seq(
		"org.webjars" % "bootstrap" % "3.0.0",
		"org.webjars" % "font-awesome" % "4.0.3",
		"org.webjars" % "angularjs" % "1.2.2",
		"org.webjars" %% "webjars-play" % "2.2.0-RC1"
	)

	val thrift = Seq(
		"org.apache.thrift" % "libthrift" % "0.9.1",
		"log4j" % "log4j" % "1.2.17",
		"ch.qos.logback" %  "logback-classic" % "1.0.6"
	)
	val elastic4s = "com.sksamuel.elastic4s" %% "elastic4s" % "1.0.0.0"
	val securesocial = ("securesocial" %% "securesocial" % "master-SNAPSHOT") exclude("org.scala-stm", "scala-stm_2.10.0")
}

object Resolvers {
	val sbt_plugins_snapshots = Resolver.url("sbt-plugin-snapshots", 
        new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/")
      )(Resolver.ivyStylePatterns)

    val bintray_masseguillaume = bintray.Opts.resolver.repo("masseguillaume", "maven")
    val sonatype_snapshots = Resolver.sonatypeRepo("snapshots")
}