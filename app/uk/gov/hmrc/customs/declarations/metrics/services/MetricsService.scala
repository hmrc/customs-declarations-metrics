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

package uk.gov.hmrc.customs.declarations.metrics.services

import java.time.Duration

import javax.inject.Inject
import com.kenshoo.play.metrics.Metrics
import play.api.mvc.Result
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.ErrorInternalServerError
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.{ConversationMetric, ConversationMetrics, EventTimeStamp, EventType}
import uk.gov.hmrc.customs.declarations.metrics.repo.MetricsRepo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Left

class MetricsService @Inject()(logger: CdsLogger, metricsRepo: MetricsRepo, val metrics: Metrics) extends HasMetrics {

  def process(conversationMetric: ConversationMetric): Future[Either[Result, Unit]] = {

    logger.debug(s"received conversation metric $conversationMetric")
    validatePayload(conversationMetric) match {
      case true =>
        conversationMetric.event.eventType match {
          case EventType("NOTIFICATION") =>
            metricsRepo.updateWithFirstNotification(conversationMetric).map { conversationMetric =>
              val originalEventType = conversationMetric.events.head.eventType
              recordTime(s"$originalEventType-round-trip", calculateElapsedTime(conversationMetric.events.head.eventStart, conversationMetric.events(1).eventEnd))
              Right(())
            }

          case EventType(eventType) =>
            metricsRepo.save(ConversationMetrics(conversationMetric.conversationId, Seq(conversationMetric.event))).map {
              case true =>
                recordTime(s"$eventType-digital", calculateElapsedTime(conversationMetric.event.eventStart, conversationMetric.event.eventEnd))
                Right(())
              case false => Left(ErrorInternalServerError.JsonResult)
            }
        }
      case false =>
        Future.successful(Left(ErrorResponse.errorBadRequest("Start date time must be before end date time").JsonResult))
    }
  }

  private def validatePayload(conversationMetric: ConversationMetric): Boolean = {
    conversationMetric.event.eventStart.zonedDateTime.isBefore(conversationMetric.event.eventEnd.zonedDateTime)
  }

  private def calculateElapsedTime(start: EventTimeStamp, end: EventTimeStamp): Duration = {
    Duration.between(start.zonedDateTime, end.zonedDateTime)
  }

}
