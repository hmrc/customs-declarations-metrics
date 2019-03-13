resolvers += Resolver.url("HMRC Sbt Plugin Releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
resolvers += "HMRC Releases" at "https://dl.bintray.com/hmrc/releases"

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "1.14.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "0.17.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "1.16.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.4")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.19")

addSbtPlugin("uk.gov.hmrc" % "sbt-settings" % "3.9.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "1.3.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-git-stamp" % "5.6.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
