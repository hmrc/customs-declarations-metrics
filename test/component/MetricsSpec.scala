/*
 * Copyright 2018 HM Revenue & Customs
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

import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, OptionValues, _}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.Helpers._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.customs.declarations.metrics.model.ConversationMetrics
import uk.gov.hmrc.customs.declarations.metrics.repo.MongoDbProvider
import uk.gov.hmrc.mongo.{Awaiting, MongoSpecSupport, ReactiveRepository}
import util.TestData.ValidRequest

import scala.concurrent.Future

class MetricsSpec extends FeatureSpec with GivenWhenThen with GuiceOneAppPerSuite
  with BeforeAndAfterAll with BeforeAndAfterEach with Eventually with Matchers with OptionValues
  with MongoSpecSupport with Awaiting{

//  private val Wait = 5
//
//  override implicit def patienceConfig: PatienceConfig = super.patienceConfig.copy(timeout = Span(Wait, Seconds))

  private val endpoint = "/log-times"

  val repo = new ReactiveRepository[ConversationMetrics, BSONObjectID](
    //TODO rename repo collection
    collectionName = "logTimes",
    mongo = app.injector.instanceOf[MongoDbProvider].mongo,
    domainFormat = ConversationMetrics.conversationMetricsJF) {  }

  val acceptanceTestConfigs: Map[String, Any] = Map(
    "auditing.enabled" -> false,
    "mongodb.uri" -> "mongodb://localhost:27017/customs-declarations-metrics"
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder().configure(acceptanceTestConfigs).build()

  override protected def beforeAll() {
    await(repo.drop)
   // startMockServer()
  }

  override protected def afterAll() {
    //stopMockServer()
    await(repo.drop)
  }

  override protected def afterEach(): Unit = {
   // resetMockServer()
  }

  override protected def beforeEach(): Unit = {

  }

  feature("Record time stamps in metrics service") {

//    scenario("Declaration Metric is received") {
//
//      Given("the API is available")
//
//      When("a POST request with data is sent to the API")
//      val result = route(app, ValidRequest)
//
//      Then("a response with a 202 status is received")
//      result shouldBe 'defined
//      val resultFuture: Future[Result] = result.value
//
//      status(resultFuture) shouldBe ACCEPTED
//
//      And("the response body is empty")
//      contentAsString(resultFuture) shouldBe 'empty
//    }
  }

}
