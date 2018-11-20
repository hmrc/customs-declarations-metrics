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

import org.scalatestplus.play._
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

class CustomsDeclarationsMetricsControllerSpec extends PlaySpec with OneAppPerTest {


  "CustomsDeclarationsMetricsController" should {

    "handle valid get and respond appropriately" in {
      val home: Future[Result] = route(app, FakeRequest(GET, "/api")).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/plain")
      contentAsString(home) must include(s"Hello World!!")
    }
  }



}
