resolvers ++= Seq(
	"Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
	"Sonatype OSS Snapshots Repository" at "http://oss.sonatype.org/content/groups/public",
	"codebrew's maven" at "http://codebrew-io.github.io/maven/"
)

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
        Resolver.ivyStylePatterns)


addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.0")

addSbtPlugin("com.twitter" %% "scrooge-sbt-plugin" % "3.9.0")

addSbtPlugin("com.jamesward" %% "play-auto-refresh" % "0.0.6")

addSbtPlugin("com.github.mumoshu" % "play2-typescript" % "0.2-RC11")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.6.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.2")