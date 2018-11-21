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

package util

import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.{ACCEPT, CONTENT_TYPE}

import scala.util.Try

object TestData {


val ValidJson = Json.parse("""
       |{
       | "eventType": "DEC-START",
       | "conversationId": "dff783d7-44ee-4836-93d0-3242da7c225f",
       | "logTimeStamp": "2014-10-23T00:35:14.123Z"
       |}
    """.stripMargin)


  val ValidRequest: FakeRequest[AnyContentAsJson] = FakeRequest("POST","/log-time")
    .withHeaders(RequestHeaders.ValidHeaders.toSeq: _*)
    .withJsonBody(ValidJson)

  val ValidRequestAsTryJsValue: Request[Try[JsValue]] = ValidRequest.copyFakeRequest[Try[JsValue]](body = Try(ValidRequest.body.json))
}


object RequestHeaders {

  val CONTENT_TYPE_HEADER: (String, String) = CONTENT_TYPE -> MimeTypes.JSON

  val CONTENT_TYPE_HEADER_INVALID: (String, String) = CONTENT_TYPE -> MimeTypes.XML

  val ACCEPT_HMRC_HEADER: (String, String) = ACCEPT -> "application/vnd.hmrc.1.0+json"

  val ACCEPT_HEADER: (String, String) = ACCEPT -> MimeTypes.JSON

  val ACCEPT_HEADER_INVALID: (String, String) = ACCEPT -> MimeTypes.XML

  val ValidHeaders = Map(
    CONTENT_TYPE_HEADER,
    ACCEPT_HMRC_HEADER)
}
