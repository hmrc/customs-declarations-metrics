import sbt._

object AppDependencies {

  private val testScope = "test,it"
  private val hmrcMongoVersion = "1.3.0"
  private val apiCommonVersion = "1.60.0"

  val compile = Seq(
    "uk.gov.hmrc"                                %% "customs-api-common" % apiCommonVersion,
    "uk.gov.hmrc.mongo"                          %% "hmrc-mongo-play-28" % hmrcMongoVersion
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "customs-api-common"      % apiCommonVersion % testScope classifier "tests",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0"          % testScope,
    "org.wiremock"            %  "wiremock-standalone"     % "3.0.2"         % testScope,
    "org.scalatestplus"      %% "mockito-3-4"             % "3.2.10.0"       % testScope,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % hmrcMongoVersion % testScope,
    "com.vladsch.flexmark"   % "flexmark-all"             % "0.35.10"        % testScope
  )
}
