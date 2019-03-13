/*
 * Copyright 2019 HM Revenue & Customs
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

package unit.controllers

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{RETURNS_DEEP_STUBS, when}
import org.scalatest.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.libs.json
import play.api.libs.json.JsValue
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.controllers.CustomsDeclarationsMetricsController
import uk.gov.hmrc.customs.declarations.metrics.model.ConversationMetric
import uk.gov.hmrc.customs.declarations.metrics.services.MetricsService
import uk.gov.hmrc.play.test.UnitSpec
import util.MockitoPassByNameHelper.PassByNameVerifier
import util.TestData._

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.util.Try

class CustomsDeclarationsMetricsControllerSpec extends UnitSpec
  with Matchers with MockitoSugar {

  trait SetUp {
    val mockLogger = mock[CdsLogger]
    val mockService = mock[MetricsService]
    val mockMessagesApi = mock[MessagesApi](RETURNS_DEEP_STUBS)
    val controller = new CustomsDeclarationsMetricsController(mockLogger, mockService, mockMessagesApi) {}

    def testSubmitResult(request: Request[Try[JsValue]])(test: Future[Result] => Unit) {
      test(controller.post().apply(request))
    }
  }

  private val wrongPayloadErrorResult = ErrorResponse(BAD_REQUEST, errorCode = "BAD_REQUEST",
    message = "Request does not contain a valid JSON body").JsonResult

  "CustomsDeclarationsMetricsController" should {

    "respond with status 406 for an invalid Accept header" in new SetUp() {
      testSubmitResult(InvalidAcceptHeaderRequest) { result =>
        status(result) shouldBe NOT_ACCEPTABLE
      }
    }

    "respond with status 406 when Accept header is not set in request" in new SetUp() {
      testSubmitResult(NoAcceptHeaderRequest) { result =>
        status(result) shouldBe NOT_ACCEPTABLE
      }
    }

    "handle valid post to log-time endpoint and respond appropriately" in new SetUp() {
      when(mockService.process(any[ConversationMetric])).thenReturn(Future.successful(Right(())))

      testSubmitResult(ValidRequestAsTryJsValue) { result =>
        status(result) shouldBe ACCEPTED
      }
    }

    "handle invalid post to log-time endpoint and respond appropriately" in new SetUp() {
      testSubmitResult(InvalidRequestAsTryJsValue) { result =>
        status(result) shouldBe BAD_REQUEST
      }

      PassByNameVerifier(mockLogger, "error")
        .withByNameParam[String]("JSON payload failed schema validation with error " +
        "JsError(List((/conversationId,List(ValidationError(List(error.path.missing),WrappedArray()))), " +
        "(/eventStart,List(ValidationError(List(error.path.missing),WrappedArray()))), " +
        "(/eventEnd,List(ValidationError(List(error.path.missing),WrappedArray())))))")
        .verify()
    }

    "respond with status 400 for a non well-formed JSON payload" in new SetUp() {
      testSubmitResult(NonJsonPayloadRequest.copyFakeRequest(body = Try(json.Json.parse("")))) { result =>
        status(result) shouldBe BAD_REQUEST
        await(result) shouldBe wrongPayloadErrorResult
      }

      PassByNameVerifier(mockLogger, "error")
        .withByNameParam[String](
          """Request does not contain a valid JSON body No content to map due to end-of-input
            | at [Source: (String)""; line: 1, column: 0]""".stripMargin)
        .verify()
    }

  }
}
