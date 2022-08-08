/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package component

import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc._
import play.api.test.Helpers._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.customs.declarations.metrics.model.ConversationMetrics
import uk.gov.hmrc.customs.declarations.metrics.repo.MongoDbProvider
import uk.gov.hmrc.mongo.{Awaiting, MongoSpecSupport, ReactiveRepository}
import util.TestData.{InvalidDateTimeStampRequest, ValidRequest}

import scala.concurrent.Future

class MetricsSpec extends AnyFeatureSpec
  with GivenWhenThen
  with GuiceOneAppPerSuite
  with BeforeAndAfterEach
  with Matchers
  with OptionValues
  with MongoSpecSupport
  with Awaiting {

  val repo = new ReactiveRepository[ConversationMetrics, BSONObjectID](
    collectionName = "metrics",
    mongo = app.injector.instanceOf[MongoDbProvider].mongo,
    domainFormat = ConversationMetrics.conversationMetricsJF) {  }

  override protected def beforeEach() {
    await(repo.drop)
  }

  override protected def afterEach() {
    await(repo.drop)
  }

  Feature("Record time stamps in metrics service") {

    Scenario("Valid Declaration Metric is received") {

      Given("the API is available")

      When("a POST request with data is sent to the API")
      val result = route(app, ValidRequest)

      Then("a response with a 202 status is received")
      result shouldBe 'defined
      val resultFuture: Future[Result] = result.value

      status(resultFuture) shouldBe ACCEPTED

      And("the response body is empty")
      contentAsString(resultFuture) shouldBe 'empty
    }


    Scenario("invalid Declaration Metric is received") {

      Given("the API is available")

      When("a POST request with data is sent to the API")
      val result = route(app, InvalidDateTimeStampRequest)

      Then("a response with a 400 status is received")
      result shouldBe 'defined
      val resultFuture: Future[Result] = result.value

      status(resultFuture) shouldBe BAD_REQUEST

      And("the response body contains the error")
      contentAsString(resultFuture) shouldBe """{"code":"BAD_REQUEST","message":"Invalid Payload"}"""
    }

  }

}
