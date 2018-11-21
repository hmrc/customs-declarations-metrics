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

package unit.controllers

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers}
import org.scalatestplus.play._
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.customs.declarations.metrics.controllers.CustomsDeclarationsMetricsController
import uk.gov.hmrc.play.test.UnitSpec
import util.TestData.ValidRequestAsTryJsValue

import scala.concurrent.Future
import scala.util.Try

class CustomsDeclarationsMetricsControllerSpec extends UnitSpec
  with Matchers with MockitoSugar with BeforeAndAfterEach {

 trait SetUp {

 }

  protected val controller = new CustomsDeclarationsMetricsController(){}

  protected def awaitSubmit(request: Request[AnyContent]): Result = {
    controller.helloWorld.apply(FakeRequest(GET, "/api"))
  }

  "CustomsDeclarationsMetricsController" should {

    "handle valid get and respond appropriately" in new SetUp() {
      val home: Future[Result] = controller.helloWorld.apply(FakeRequest(GET, "/api"))

      status(home) shouldBe OK
      contentType(home) shouldBe Some("text/plain")
      contentAsString(home) should include("Hello World!!")
    }

    "handle valid post to log-times endpoint and respond appropriately" in {

     testSubmitResult(ValidRequestAsTryJsValue) { result =>
        status(result) shouldBe ACCEPTED
      }
    }
  }

  private def testSubmitResult(request: Request[Try[JsValue]])(test: Future[Result] => Unit) {
    test(controller.post().apply(request))
  }

}
