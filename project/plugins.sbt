resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.4")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")