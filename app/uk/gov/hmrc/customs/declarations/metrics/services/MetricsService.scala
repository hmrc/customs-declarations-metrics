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

package uk.gov.hmrc.customs.declarations.metrics.services

import java.time.{Duration, ZoneId, ZonedDateTime}
import javax.inject.{Inject, Singleton}

import com.kenshoo.play.metrics.Metrics
import play.api.mvc.Result
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.ErrorInternalServerError
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model._
import uk.gov.hmrc.customs.declarations.metrics.repo.MetricsRepo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Left

@Singleton
class MetricsService @Inject()(val logger: CdsLogger, metricsRepo: MetricsRepo, val metrics: Metrics) extends HasMetrics {

  def process(conversationMetric: ConversationMetric): Future[Either[Result, Unit]] = {

    logger.debug(s"received conversation metric $conversationMetric")

    if (validatePayload(conversationMetric)) {
      conversationMetric.event.eventType match {
        case EventType("NOTIFICATION") =>
          metricsRepo.updateWithFirstNotification(conversationMetric).map { conversationMetrics =>
            val originalEventType = conversationMetrics.events.head.eventType
            recordTime(s"${originalEventType.eventTypeString.toLowerCase}-round-trip", calculateElapsedTime(conversationMetrics.events.head.eventStart, conversationMetrics.events(1).eventEnd))
            recordTime("notification-digital", calculateElapsedTime(conversationMetric.event.eventStart, conversationMetric.event.eventEnd))
            recordTime(s"${originalEventType.eventTypeString.toLowerCase}-digital-total", calculateDigitalElapsedTime(conversationMetrics.events.head, conversationMetric.event))
            Right(())
          }.recover {
            case _: Throwable => Right(())
          }
        case EventType(eventType) =>
          metricsRepo.save(ConversationMetrics(conversationMetric.conversationId, Seq(conversationMetric.event), ZonedDateTime.now(ZoneId.of("UTC")))).map {
            case true =>
              recordTime(s"${eventType.toLowerCase}-digital", calculateElapsedTime(conversationMetric.event.eventStart, conversationMetric.event.eventEnd))
              Right(())
            case false => Left(ErrorInternalServerError.JsonResult)
          }.recover {
            case e: Throwable =>
              logger.error(s"failed saving metric: ${e.getMessage}")
              Right(())
          }
      }
    } else {
      logger.error(s"Invalid payload $conversationMetric")
      Future.successful(Left(ErrorResponse.errorBadRequest("Invalid Payload").JsonResult))
    }
  }

  private def validatePayload(conversationMetric: ConversationMetric): Boolean = {
    conversationMetric.event.eventType.eventTypeString.isEmpty ||
    conversationMetric.event.eventStart.isBefore(conversationMetric.event.eventEnd)
  }

  private def calculateDigitalElapsedTime(declarationEvent: Event, notificationEvent: Event): Duration ={
    calculateElapsedTime(declarationEvent.eventStart,declarationEvent.eventEnd).plus(calculateElapsedTime(notificationEvent.eventStart, notificationEvent.eventEnd))
  }

  private def calculateElapsedTime(start: ZonedDateTime, end: ZonedDateTime): Duration = {
    Duration.between(start, end)
  }

}
