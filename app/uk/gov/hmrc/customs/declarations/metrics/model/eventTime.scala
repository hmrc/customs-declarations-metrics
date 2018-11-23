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

import java.time.ZonedDateTime
import java.util.UUID

import play.api.libs.json._
import play.api.libs.functional.syntax._

//TODO consider simplifying some of the serialisation below
//TODO consider converting to enum
case class EventType(eventTypeString: String) extends AnyVal
object EventType {
  implicit val eventTypeJF: Format[EventType] = new Format[EventType] {
    def writes(eventType: EventType) = JsString(eventType.eventTypeString)
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

case class EventTimeStamp(zonedDateTime: ZonedDateTime) extends AnyVal {
  override def toString: String = zonedDateTime.toString
}

object EventTimeStamp {
  implicit val eventTimeStampJF: Format[EventTimeStamp] = new Format[EventTimeStamp] {
    def writes(zonedDateTime: EventTimeStamp) = JsString(zonedDateTime.toString)
    def reads(json: JsValue): JsResult[EventTimeStamp] = json match {
      case JsNull => JsError()
      case _ => JsSuccess(EventTimeStamp(json.as[ZonedDateTime]))
    }
  }
}

case class Event(eventType: EventType, eventStart: EventTimeStamp, eventEnd: EventTimeStamp)
object Event {
  implicit val eventReads: Reads[Event] = (
    (JsPath \ "eventType").read[EventType] and
    (JsPath \ "eventStart").read[EventTimeStamp] and
    (JsPath \ "eventEnd").read[EventTimeStamp]) (Event.apply _)
  implicit val eventWrites: OWrites[Event] = (
    (JsPath \ "eventType").write[EventType] and
    (JsPath \ "eventStart").write[EventTimeStamp] and
    (JsPath \ "eventEnd").write[EventTimeStamp]) (unlift(Event.unapply))

  implicit val EventJF: Format[Event] = Format(eventReads, eventWrites)
}

case class ConversationMetric(conversationId: ConversationId, event: Event)
object ConversationMetric {
  implicit val conversationMetricReads: Reads[ConversationMetric] = (
    (JsPath \ "conversationId").read[ConversationId] and
    Event.eventReads
  )(ConversationMetric.apply _)

  implicit val conversationMetricWrites: OWrites[ConversationMetric] =(
    (JsPath \ "conversationId").write[ConversationId] and
    Event.eventWrites) (unlift(ConversationMetric.unapply))

  implicit val conversationMetricJF: Format[ConversationMetric] = Format(conversationMetricReads, conversationMetricWrites)

}

case class ConversationMetrics(conversationId: ConversationId, events: Seq[Event])
object ConversationMetrics {
  implicit val eventsReads: Reads[Seq[Event]] = Reads.seq(Event.eventReads)
  implicit val eventsWrites: Writes[Seq[Event]] = Writes.seq(Event.eventWrites)

  implicit val conversationMetricsJF = Json.format[ConversationMetrics]
}
