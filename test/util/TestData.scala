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

import java.time.LocalDateTime
import java.util.UUID

import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, AnyContentAsText, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.{ACCEPT, CONTENT_TYPE}
import uk.gov.hmrc.customs.declarations.metrics.model.{ConversationId, EventType, EventTime, LogTimeStamp}

import scala.util.Try

object TestData {

  val EventType1 = EventType("DEC-START")
  val ConversationId1 = ConversationId(UUID.fromString("dff783d7-44ee-4836-93d0-3242da7c225f"))
  val LogTimeStamp1 = LogTimeStamp(LocalDateTime.now().minusMinutes(2))
  val EventTime1 = EventTime(EventType1, ConversationId1, LogTimeStamp1)

  val EventType2 = EventType("DEC-START")
  val ConversationId2 = ConversationId(UUID.fromString("153d8350-10df-4bd7-b6ad-636450e7fda1"))
  val LogTimeStamp2 = LogTimeStamp(LocalDateTime.now().minusMinutes(1))
  val EventTime2 = EventTime(EventType2, ConversationId2, LogTimeStamp2)

  val EventType3 = EventType("DEC-START")
  val ConversationId3 = ConversationId(UUID.fromString("ebd5998d-f655-4a54-a309-c98ee18cf944"))
  val LogTimeStamp3 = LogTimeStamp(LocalDateTime.now())
  val EventTime3 = EventTime(EventType3, ConversationId3, LogTimeStamp3)

  val ValidJson: JsValue = Json.parse("""
       |{
       | "eventType": "DEC-START",
       | "conversationId": "dff783d7-44ee-4836-93d0-3242da7c225f",
       | "logTimeStamp": "2014-10-23T00:35:14.123Z"
       |}
    """.stripMargin)

  val InvalidJson: JsValue = Json.parse("""
       |{
       | "eventType": "DEC-START"
       |}
    """.stripMargin)

  val NonJsonPayload: String = "This is a non-json payload"

  val ValidRequest: FakeRequest[AnyContentAsJson] = FakeRequest("POST","/log-time")
    .withHeaders(RequestHeaders.ValidHeaders.toSeq: _*)
    .withJsonBody(ValidJson)

  val InvalidRequest: FakeRequest[AnyContentAsJson] = FakeRequest("POST","/log-time")
    .withHeaders(RequestHeaders.ValidHeaders.toSeq: _*)
    .withJsonBody(InvalidJson)

  val InvalidAcceptHeaderRequest: FakeRequest[Try[JsValue]] = FakeRequest()
    .withHeaders(RequestHeaders.ACCEPT_HEADER_INVALID, RequestHeaders.CONTENT_TYPE_HEADER).withBody(Try(ValidJson))

  val NoAcceptHeaderRequest: FakeRequest[Try[JsValue]] = FakeRequest()
    .withHeaders(RequestHeaders.CONTENT_TYPE_HEADER).withBody(Try(ValidJson))

  val ValidRequestAsTryJsValue: Request[Try[JsValue]] = ValidRequest.copyFakeRequest[Try[JsValue]](body = Try(ValidRequest.body.json))
  val InvalidRequestAsTryJsValue: Request[Try[JsValue]] = InvalidRequest.copyFakeRequest[Try[JsValue]](body = Try(InvalidRequest.body.json))
  val NonJsonPayloadRequest: FakeRequest[AnyContentAsText] = ValidRequest.withTextBody(NonJsonPayload)

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
