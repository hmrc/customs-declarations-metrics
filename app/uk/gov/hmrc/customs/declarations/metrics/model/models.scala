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

package uk.gov.hmrc.customs.declarations.metrics.model

import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.ZonedDateTime
import java.util.UUID

object RequestReads {
  val dateTimeRequestReads: Reads[ZonedDateTime] = JsPath.read[String].map { zonedDateTime =>
    ZonedDateTime.parse(zonedDateTime)
  }

  val eventRequestReads: Reads[Event] = (
    (__ \ "eventType").read[EventType] and
    (__ \ "eventStart").read[ZonedDateTime](dateTimeRequestReads) and
    (__ \ "eventEnd").read[ZonedDateTime](dateTimeRequestReads)) (Event.apply _)

  val conversationMetricRequestReads: Reads[ConversationMetric] = (
    (__ \ "conversationId").read[ConversationId] and eventRequestReads
    )(ConversationMetric.apply _)
}


case class EventType(eventTypeString: String) extends AnyVal
object EventType {
  implicit val eventTypeJF: Format[EventType] = new Format[EventType] {
    def writes(eventType: EventType): JsString = JsString(eventType.eventTypeString)
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
    def writes(conversationId: ConversationId): JsString = JsString(conversationId.id.toString)
    def reads(json: JsValue): JsResult[ConversationId] = json match {
      case JsNull => JsError()
      case _ => JsSuccess(ConversationId(json.as[UUID]))
    }
  }
}

case class Event(eventType: EventType, eventStart: ZonedDateTime, eventEnd: ZonedDateTime)
object Event {
  implicit val dateTimeFormats: Format[ZonedDateTime] = ModelsZonedDateTimeFormat.mongoZonedDateTimeFormat

  implicit val EventJF: Format[Event] = Json.format[Event]
}

case class ConversationMetric(conversationId: ConversationId, event: Event)
object ConversationMetric {
  implicit val dateTimeFormats: Format[ZonedDateTime] = ModelsZonedDateTimeFormat.mongoZonedDateTimeFormat
  implicit val conversationMetricJF: Format[ConversationMetric] = Json.format[ConversationMetric]
}

case class ConversationMetrics(conversationId: ConversationId, events: Seq[Event], createdDate: ZonedDateTime)
object ConversationMetrics {

  implicit val dateTimeFormats: Format[ZonedDateTime] = ModelsZonedDateTimeFormat.mongoZonedDateTimeFormat
  implicit val eventsReads: Reads[Seq[Event]] = Reads.seq[Event]
  implicit val eventsWrites: Writes[Seq[Event]] = Writes.seq[Event]

  implicit val conversationMetricsJF: OFormat[ConversationMetrics] = Json.format[ConversationMetrics]
}
