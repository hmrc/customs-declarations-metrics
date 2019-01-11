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

package util

import java.time._
import java.util.UUID

import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, AnyContentAsText, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.{ACCEPT, CONTENT_TYPE}
import uk.gov.hmrc.customs.declarations.metrics.model._

import scala.util.Try

object TestData {

  val DeclarationEventType = EventType("DECLARATION")
  val DeclarationConversationId = ConversationId(UUID.fromString("dff783d7-44ee-4836-93d0-3242da7c225f"))
  val DeclarationTimeStampEntry = ZonedDateTime.parse("2014-10-23T00:35:14.123Z")
  val DeclarationTimeStampExit = DeclarationTimeStampEntry.plusSeconds(2)
  val DeclarationEvent = Event(DeclarationEventType, DeclarationTimeStampEntry, DeclarationTimeStampExit)
  val DeclarationConversationMetric = ConversationMetric(DeclarationConversationId, DeclarationEvent)

  val NotificationEventType = EventType("NOTIFICATION")
  val NotificationConversationId = ConversationId(UUID.fromString("153d8350-10df-4bd7-b6ad-636450e7fda1"))
  val NotificationTimeStampEntry = ZonedDateTime.parse("2014-10-23T00:36:14.123Z")
  val NotificationTimeStampExit = NotificationTimeStampEntry.plusSeconds(4)
  val NotificationEvent = Event(NotificationEventType, NotificationTimeStampEntry, NotificationTimeStampExit)
  val NotificationConversationMetric = ConversationMetric(DeclarationConversationId, NotificationEvent)

  val ConversationMetricsWithDeclarationEventOnly = ConversationMetrics(DeclarationConversationId, Seq(DeclarationEvent), ZonedDateTime.now(ZoneId.of("UTC")))
  val ConversationMetricsWithNotificationEventOnly = ConversationMetrics(DeclarationConversationId, Seq(NotificationEvent), ZonedDateTime.now(ZoneId.of("UTC")))
  val ConversationMetrics1 = ConversationMetrics(DeclarationConversationId, Seq(DeclarationEvent, NotificationEvent), ZonedDateTime.now(ZoneId.of("UTC")))

  val ValidJson: JsValue = Json.parse("""
       |{
       | "eventType": "DECLARATION",
       | "conversationId": "dff783d7-44ee-4836-93d0-3242da7c225f",
       | "eventStart": "2014-10-23T00:35:14.123Z",
       | "eventEnd": "2014-10-23T00:36:14.123Z"
       |}
    """.stripMargin)

  val InvalidDateTimeStampsJson: JsValue = Json.parse("""
      |{
      | "eventType": "DECLARATION",
      | "conversationId": "dff783d7-44ee-4836-93d0-3242da7c225f",
      | "eventStart": "2014-10-23T00:35:14.123Z",
      | "eventEnd": "2014-10-21T00:36:14.123Z"
      |}
    """.stripMargin)

  val InvalidJson: JsValue = Json.parse("""
       |{
       | "eventType": "DECLARATION"
       |}
    """.stripMargin)

  val NonJsonPayload: String = "This is a non-json payload"

  val ValidRequest: FakeRequest[AnyContentAsJson] = FakeRequest("POST","/log-times")
    .withHeaders(RequestHeaders.ValidHeaders.toSeq: _*)
    .withJsonBody(ValidJson)

  val InvalidRequest: FakeRequest[AnyContentAsJson] = FakeRequest("POST","/log-times")
    .withHeaders(RequestHeaders.ValidHeaders.toSeq: _*)
    .withJsonBody(InvalidJson)

  val InvalidDateTimeStampRequest: FakeRequest[AnyContentAsJson] = FakeRequest("POST","/log-times")
    .withHeaders(RequestHeaders.ValidHeaders.toSeq: _*)
    .withJsonBody(InvalidDateTimeStampsJson)

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
