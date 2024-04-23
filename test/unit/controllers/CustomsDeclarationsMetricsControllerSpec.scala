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

package unit.controllers

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{RETURNS_DEEP_STUBS, when}
import play.api.i18n.MessagesApi
import play.api.libs.json.JsValue
import play.api.mvc.{ControllerComponents, Request, Result}
import play.api.test.Helpers
import play.api.test.Helpers._
import uk.gov.hmrc.customs.declarations.metrics.common.controllers.ErrorResponse
import uk.gov.hmrc.customs.declarations.metrics.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.controllers.CustomsDeclarationsMetricsController
import uk.gov.hmrc.customs.declarations.metrics.model.ConversationMetric
import uk.gov.hmrc.customs.declarations.metrics.services.MetricsService
import util.UnitSpec
import util.MockitoPassByNameHelper.PassByNameVerifier
import util.TestData._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CustomsDeclarationsMetricsControllerSpec extends UnitSpec {

  trait SetUp {
    private implicit val ec: ExecutionContext = Helpers.stubControllerComponents().executionContext
    val mockLogger: CdsLogger = mock[CdsLogger]
    val mockService: MetricsService = mock[MetricsService]
    val cc: ControllerComponents = stubControllerComponents()
    val mockMessagesApi: MessagesApi = mock[MessagesApi](RETURNS_DEEP_STUBS)
    val controller: CustomsDeclarationsMetricsController = new CustomsDeclarationsMetricsController(mockLogger, mockService, cc, mockMessagesApi) {}

    def testSubmitResult(request: Request[Try[JsValue]])(test: Future[Result] => Unit): Unit = {
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
        "JsError(List((/conversationId,List(JsonValidationError(List(error.path.missing),List()))), " +
        "(/eventStart,List(JsonValidationError(List(error.path.missing),List()))), " +
        "(/eventEnd,List(JsonValidationError(List(error.path.missing),List())))))")
        .verify()
    }

    "respond with status 400 for a non well-formed JSON payload" in new SetUp() {
      testSubmitResult(NoJsonPayloadRequest) { result =>
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
