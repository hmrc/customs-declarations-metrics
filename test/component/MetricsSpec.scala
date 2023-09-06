/*
 * Copyright 2023 HM Revenue & Customs
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

import org.mockito.Mockito.when
import org.mongodb.scala.bson.Document
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc._
import play.api.test.Helpers
import play.api.test.Helpers._
import uk.gov.hmrc.customs.declarations.metrics.model.MetricsConfig
import uk.gov.hmrc.customs.declarations.metrics.repo.MetricsMongoRepo
import util.TestData.{InvalidDateTimeStampRequest, ValidRequest}

import scala.concurrent.{ExecutionContext, Future}

class MetricsSpec extends AnyWordSpecLike
  with EitherValues
  with GuiceOneAppPerSuite
  with BeforeAndAfterEach
  with Matchers
  with OptionValues {

  lazy val repository: MetricsMongoRepo = app.injector.instanceOf[MetricsMongoRepo]

  implicit val ec: ExecutionContext = Helpers.stubControllerComponents().executionContext

  private val mockMetricsConfig = mock[MetricsConfig]
  val twoWeeksInSeconds = 1209600

  override protected def beforeEach(): Unit = {
    await(repository.collection.deleteMany(Document.empty).toFuture())
    when(mockMetricsConfig.ttlInSeconds).thenReturn(twoWeeksInSeconds)
  }

  override protected def afterEach(): Unit = {
    await(repository.collection.deleteMany(Document.empty).toFuture())
  }

  "POST request with data is sent to the API" should {
    "Record time stamps in metrics collection, respond with a 202 & empty response body" when {
      "Valid Declaration Metric is received" in {

        val result = route(app, ValidRequest)

        result shouldBe defined
        val resultFuture: Future[Result] = result.value
        status(resultFuture) shouldBe ACCEPTED

        contentAsString(resultFuture) shouldBe empty
      }
    }


    "Record time stamps in metrics collection, respond with a 400 & response body contains the error" when {
      "invalid Declaration Metric is received" in {


        val result = route(app, InvalidDateTimeStampRequest)

        result shouldBe defined
        val resultFuture: Future[Result] = result.value

        status(resultFuture) shouldBe BAD_REQUEST

        contentAsString(resultFuture) shouldBe """{"code":"BAD_REQUEST","message":"Invalid Payload"}"""
      }

    }
  }

}
