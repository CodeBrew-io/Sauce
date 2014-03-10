resolvers ++= Seq(
	"bigtoast-github" at "http://bigtoast.github.com/repo/",
	"Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
	"Sonatype OSS Snapshots Repository" at "http://oss.sonatype.org/content/groups/public",
	"codebrew's maven" at "http://codebrew-io.github.io/maven/",
	Resolver.url(
		"bintray-sbt-plugin-releases", 
		url("http://dl.bintray.com/content/sbt/sbt-plugin-releases")
	)(Resolver.ivyStylePatterns)
)

addSbtPlugin("com.github.bigtoast" % "sbt-thrift" % "0.7")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.6.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.2")