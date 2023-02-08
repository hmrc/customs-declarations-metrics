import sbt.{compilerPlugin, _}

object AppDependencies {

  private val testScope = "test,it"
  private val hmrcMongoVersion = "0.74.0"

  val compile = Seq(
    "uk.gov.hmrc"                                %% "customs-api-common" % "1.57.0",
    "uk.gov.hmrc.mongo"                          %% "hmrc-mongo-play-28" % hmrcMongoVersion,
    "com.github.ghik"                            %  "silencer-lib"       % "1.7.12" % Provided cross CrossVersion.full,
    compilerPlugin("com.github.ghik" %  "silencer-plugin"    % "1.7.12" cross CrossVersion.full),
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "customs-api-common"      % "1.57.0"         % testScope classifier "tests",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0"          % testScope,
    "com.github.tomakehurst" %  "wiremock-standalone"     % "2.27.2"         % testScope,
    "org.scalatestplus"      %% "mockito-3-4"             % "3.2.10.0"        % testScope,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % hmrcMongoVersion % testScope,
    "com.vladsch.flexmark"   % "flexmark-all"             % "0.35.10"        % testScope
  )
}
