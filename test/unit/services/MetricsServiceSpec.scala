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

package unit.services


import java.time.Duration

import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.Metrics
import org.mockito.Mockito.{RETURNS_DEEP_STUBS, verify, when}
import org.mockito.ArgumentMatchers.any
import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.ErrorInternalServerError
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.{ConversationMetric, ConversationMetrics}
import uk.gov.hmrc.customs.declarations.metrics.repo.MetricsRepo
import uk.gov.hmrc.customs.declarations.metrics.services.MetricsService
import uk.gov.hmrc.play.test.UnitSpec
import util.TestData.{DeclarationConversationMetric, ConversationMetrics1, NotificationConversationMetric}

import scala.concurrent.Future

class MetricsServiceSpec extends UnitSpec
  with Matchers with MockitoSugar {

  trait SetUp {
    val mockMetrics = mock[Metrics]
    val mockLogger = mock[CdsLogger]
    val mockRepo = mock[MetricsRepo]
    val mockMetricsRegistry = mock[MetricRegistry](RETURNS_DEEP_STUBS)

    when(mockMetrics.defaultRegistry).thenReturn(mockMetricsRegistry)
    val service = new MetricsService(mockLogger, mockRepo, mockMetrics){
      override lazy val registry: MetricRegistry = mock[MetricRegistry]
      override def recordTime(timerName: Metric, duration: Duration): Unit = {
        ()
      }

    }
  }

  "MetricsService" should {

    "save a declarationMetric successfully" in new SetUp() {
      when(mockRepo.save(any[ConversationMetrics])).thenReturn(Future.successful(true))

      val result = service.process(DeclarationConversationMetric)

      verify(mockRepo).save(any[ConversationMetrics])
      await(result) shouldBe Right(())
    }

    "save a declarationMetric returns an errorResponse when save fails" in new SetUp() {
      when(mockRepo.save(any[ConversationMetrics])).thenReturn(Future.successful(false))

      val result = service.process(DeclarationConversationMetric)

      verify(mockRepo).save(any[ConversationMetrics])
      await(result) shouldBe Left(ErrorInternalServerError)
    }

    "save a notification metric successfully" in new SetUp() {
      when(mockRepo.updateWithFirstNotification(any[ConversationMetric])).thenReturn(Future.successful(ConversationMetrics1))

      val result = service.process(NotificationConversationMetric)

      verify(mockRepo).updateWithFirstNotification(any[ConversationMetric])
      await(result) shouldBe Right(())
    }

    "save a notification metric throws an Exception when update fails " in new SetUp() {
      when(mockRepo.updateWithFirstNotification(any[ConversationMetric])).thenThrow(new IllegalStateException("Some Error"))

      val caught = intercept[IllegalStateException](await(service.process(NotificationConversationMetric)))

      verify(mockRepo).updateWithFirstNotification(any[ConversationMetric])
      caught.getMessage shouldBe "Some Error"
    }

  }


}
