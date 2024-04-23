import sbt._

object AppDependencies {

  val playSuffix       = "-play-30"
  val bootstrapVersion = "8.0.0"
  val hmrcMongoVersion = "1.8.0"
  val scalamockVersion = "5.2.0"

  val compile = Seq(
    "uk.gov.hmrc"        %% s"bootstrap-backend$playSuffix" % bootstrapVersion,
    "org.typelevel"      %% "cats-core"                     % "2.10.0",
    "uk.gov.hmrc.mongo"  %% s"hmrc-mongo$playSuffix"        % hmrcMongoVersion,
    "com.typesafe.play"  %% "play-json-joda"                % "2.8.1"
  )

  val test = Seq(
    "org.scalatestplus"  %% "scalatestplus-mockito"        % "1.0.0-M2" ,
    "uk.gov.hmrc"        %% s"bootstrap-test$playSuffix"   % bootstrapVersion,
    "uk.gov.hmrc.mongo"  %% s"hmrc-mongo-test$playSuffix"  % hmrcMongoVersion,
    "org.scalamock"      %% "scalamock"                    % scalamockVersion
  ).map(_ % Test)
}


  val playSuffix = "-play-30"

  val bootstrapVersion = "8.0.0"
  val hmrcMongoVersion = "1.8.0"
  val scalamockVersion = "5.2.0"

  val compile = Seq(
    "uk.gov.hmrc"                                %% s"bootstrap-backend$playSuffix" % bootstrapVersion,
    "org.typelevel"                              %% "cats-core"                    % "2.10.0",
    "uk.gov.hmrc.mongo"                          %% s"hmrc-mongo$playSuffix"        % hmrcMongoVersion,
    "com.typesafe.play"                          %% "play-json-joda"               % "2.8.1"
  )

  val test = Seq(
    "org.scalatestplus"                          %% "scalatestplus-mockito"        % "1.0.0-M2"                      % Test,
    "uk.gov.hmrc"                                %% s"bootstrap-test$playSuffix"    % bootstrapVersion                % Test,
    "uk.gov.hmrc.mongo"                          %% s"hmrc-mongo-test$playSuffix"   % hmrcMongoVersion                % Test,
    "org.scalamock"                              %% "scalamock"                    % scalamockVersion                % Test
  )
}
