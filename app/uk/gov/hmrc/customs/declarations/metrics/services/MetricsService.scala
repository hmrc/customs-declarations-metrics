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
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.{ConversationMetric, ConversationMetrics, EventTimeStamp, EventType}
import uk.gov.hmrc.customs.declarations.metrics.repo.MetricsRepo

import scala.concurrent.Future

class MetricsService @Inject()(logger: CdsLogger, metricsRepo: MetricsRepo, val metrics: Metrics) extends HasMetrics {

  def process(conversationMetric: ConversationMetric): Future[Either[ErrorResponse, Boolean]] = {

    conversationMetric.event.eventType match {
      case EventType("DECLARATION") =>
        //DECLARATION => store in Mongo, calc elapsed time from two timestamps & store duration in graphite
        //TODO check boolean returned
        val success = metricsRepo.save(ConversationMetrics(conversationMetric.conversationId, Seq(conversationMetric.event)))
        recordTime("declaration-digital", calculateElapsedTime(conversationMetric.event.eventStart, conversationMetric.event.eventEnd))
        Future.successful(Right(true))

      case EventType("NOTIFICATION") =>
        //NOTIFICATION => find & update mongo rec only where no cn previously received, calc round trip time & store duration in graphite

      ???

    }

  }

  def calculateElapsedTime(start: EventTimeStamp, end: EventTimeStamp): Duration = {
    Duration.between(start.zonedDateTime, end.zonedDateTime)
  }

}
