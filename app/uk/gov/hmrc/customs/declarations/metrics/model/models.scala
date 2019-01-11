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

package uk.gov.hmrc.customs.declarations.metrics.model

import java.time.{Instant, ZoneId, ZonedDateTime}
import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json._

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

object DateTimeFormats {
  //"$date" in reads and writes used to preserve date type, part of MongoDB Extended JSON.
  //Read more here - http://reactivemongo.org/releases/0.1x/documentation/json/overview.html#documents-and-values
  val dateTimeReads: Reads[ZonedDateTime] =
    (__ \ "$date").read[Long].map { zonedDateTime =>
      val instant = Instant.ofEpochMilli(zonedDateTime)
      ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"))
    }

  implicit val dateTimeWrite: Writes[ZonedDateTime] = new Writes[ZonedDateTime] {
    def writes(zonedDateTime: ZonedDateTime): JsValue = Json.obj("$date" -> zonedDateTime.toInstant.toEpochMilli
    )
  }

  implicit val dateTimeJF = Format(dateTimeReads, dateTimeWrite)
}

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

case class Event(eventType: EventType, eventStart: ZonedDateTime, eventEnd: ZonedDateTime)
object Event {
  implicit val dateTimeFormats = DateTimeFormats.dateTimeJF
  implicit val eventReads: Reads[Event] = (
    (__ \ "eventType").read[EventType] and
    (__ \ "eventStart").read[ZonedDateTime] and
    (__ \ "eventEnd").read[ZonedDateTime]) (Event.apply _)
  implicit val eventWrites: OWrites[Event] = (
    (__ \ "eventType").write[EventType] and
    (__ \ "eventStart").write[ZonedDateTime] and
    (__ \ "eventEnd").write[ZonedDateTime]) (unlift(Event.unapply))

  implicit val EventJF: Format[Event] = Format(eventReads, eventWrites)
}

case class ConversationMetric(conversationId: ConversationId, event: Event)
object ConversationMetric {
  implicit val dateTimeFormats = DateTimeFormats.dateTimeJF
  implicit val conversationMetricReads: Reads[ConversationMetric] = (
    (__ \ "conversationId").read[ConversationId] and
    Event.eventReads
  )(ConversationMetric.apply _)

  implicit val conversationMetricWrites: OWrites[ConversationMetric] =(
    (__ \ "conversationId").write[ConversationId] and
      Event.eventWrites) (unlift(ConversationMetric.unapply))

  implicit val conversationMetricJF: Format[ConversationMetric] = Format(conversationMetricReads, conversationMetricWrites)
}

case class ConversationMetrics(conversationId: ConversationId, events: Seq[Event], createdDate: ZonedDateTime)
object ConversationMetrics {

  implicit val dateTimeFormats = DateTimeFormats.dateTimeJF
  implicit val eventsReads: Reads[Seq[Event]] = Reads.seq(Event.eventReads)
  implicit val eventsWrites: Writes[Seq[Event]] = Writes.seq(Event.eventWrites)

  implicit val conversationMetricsJF: OFormat[ConversationMetrics] = Json.format[ConversationMetrics]
}
