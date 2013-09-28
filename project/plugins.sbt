logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.0")

//resolvers += "gui maven" at "http://masseguillaume.github.io/maven"

addSbtPlugin("com.twitter" %% "scrooge-sbt-plugin" % "3.9.0")