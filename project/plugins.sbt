logLevel := Level.Warn

resolvers ++= Seq(
	"Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
	"Sonatype OSS Snapshots Repository" at "http://oss.sonatype.org/content/groups/public",
	"gui maven" at "http://masseguillaume.github.io/maven"
)

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.0")

addSbtPlugin("com.twitter" %% "scrooge-sbt-plugin" % "3.9.0")

addSbtPlugin("com.jamesward" %% "play-auto-refresh" % "0.0.5")

addSbtPlugin("com.github.mumoshu" % "play2-typescript" % "0.2-RC10")