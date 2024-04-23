import sbt._

object AppDependencies {

  val playSuffix       = "-play-30"
  val bootstrapVersion = "8.5.0"
  val hmrcMongoVersion = "1.9.0"
  val scalamockVersion = "6.0.0"

  val compile = Seq(
    "uk.gov.hmrc"        %% s"bootstrap-backend$playSuffix" % bootstrapVersion,
    "org.typelevel"      %% "cats-core"                     % "2.10.0",
    "uk.gov.hmrc.mongo"  %% s"hmrc-mongo$playSuffix"        % hmrcMongoVersion,
  )

  val test = Seq(
    "uk.gov.hmrc"        %% s"bootstrap-test$playSuffix"   % bootstrapVersion,
    "uk.gov.hmrc.mongo"  %% s"hmrc-mongo-test$playSuffix"  % hmrcMongoVersion,
  ).map(_ % Test)
}