package uk.gov.hmrc.customs.declarations.metrics.controllers

import java.time.LocalDateTime
import java.util.UUID

import play.api.libs.json._

case class EventType(eventTypeString : String)
object EventType {
  implicit val eventTypeJF: OFormat[EventType] = Json.format[EventType]
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

//case class LogTimeStamp(localDateTime: LocalDateTime) extends AnyVal {
//  override def toString: String = localDateTime.toString
//}
//
//object LogTimeStamp {
//  implicit val dateFormats: OFormat[LogTimeStamp] = Json.format[LogTimeStamp]
//}

case class LogTimeRequest(eventType: EventType)

object LogTimeRequest {
  implicit val formats: OFormat[LogTimeRequest] = Json.format[LogTimeRequest]
}

