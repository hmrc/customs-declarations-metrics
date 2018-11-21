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

package uk.gov.hmrc.customs.declarations.metrics.model

import java.time.LocalDateTime
import java.util.UUID

import play.api.libs.json._

case class EventType(eventTypeString: String)
object EventType {
implicit val eventTypeJF: Format[EventType] = new Format[EventType] {
  def writes(eventTypeString: EventType) = JsString(eventTypeString.toString)
  def reads(json: JsValue): JsResult[EventType] = json match {
    case JsNull => JsError()
    case _ => JsSuccess(EventType(json.as[String]))
  }
}

}

case class ConversationId(id: UUID) extends AnyVal {
  override def toString: String = id.toString
}
object ConversationId {
  implicit val conversationIdJF: Format[ConversationId] = new Format[ConversationId] {
    def writes(conversationId: ConversationId) = JsString(conversationId.id.toString)
    def reads(json: JsValue): JsResult[ConversationId] = json match {
      case JsNull => JsError()
      case _ => JsSuccess(ConversationId(json.as[UUID]))
    }
  }
}

case class LogTimeStamp(localDateTime: LocalDateTime) extends AnyVal {
  override def toString: String = localDateTime.toString
}

object LogTimeStamp {
  implicit val logTimeStampJF: Format[LogTimeStamp] = new Format[LogTimeStamp] {
    def writes(localDateTime: LogTimeStamp) = JsString(localDateTime.toString)
    def reads(json: JsValue): JsResult[LogTimeStamp] = json match {
      case JsNull => JsError()
      case _ => JsSuccess(LogTimeStamp(json.as[LocalDateTime]))
    }
  }
}

case class LogTimeRequest(eventType: EventType, conversationId: ConversationId, logTimeStamp: LogTimeStamp)

object LogTimeRequest {
  implicit val LogTimeRequestJF: OFormat[LogTimeRequest] = Json.format[LogTimeRequest]
}
