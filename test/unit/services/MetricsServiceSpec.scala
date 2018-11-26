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

import com.codahale.metrics.{Metric, MetricRegistry}
import com.kenshoo.play.metrics.Metrics
import org.mockito.Mockito.{RETURNS_DEEP_STUBS, verify, when}
import org.mockito.ArgumentMatchers.any
import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.ErrorInternalServerError
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.{ConversationMetric, ConversationMetrics}
import uk.gov.hmrc.customs.declarations.metrics.repo.MetricsRepo
import uk.gov.hmrc.customs.declarations.metrics.services.{HasMetrics, MetricsService}
import uk.gov.hmrc.play.test.UnitSpec
import util.TestData.{ConversationMetrics1, DeclarationConversationMetric, NotificationConversationMetric}

import scala.concurrent.Future

class MetricsServiceSpec extends UnitSpec
  with Matchers with MockitoSugar {

  trait FakeHasMetrics extends HasMetrics{
   var recordTimeArgumentCaptor = Map[String, Duration]()
    override lazy val registry: MetricRegistry = mock[MetricRegistry]
    override def recordTime(timerName: Metric, duration: Duration): Unit = {
      recordTimeArgumentCaptor = Map(timerName -> duration)
    }
  }

  trait SetUp {
    val mockMetrics = mock[Metrics]
    val mockLogger = mock[CdsLogger]
    val mockRepo = mock[MetricsRepo]
    val mockMetricsRegistry = mock[MetricRegistry](RETURNS_DEEP_STUBS)



    when(mockMetrics.defaultRegistry).thenReturn(mockMetricsRegistry)
    val service = new MetricsService(mockLogger, mockRepo, mockMetrics) with FakeHasMetrics
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

      val result = await(service.process(NotificationConversationMetric))
      service.recordTimeArgumentCaptor.size shouldBe 1
      service.recordTimeArgumentCaptor.head._1 shouldBe "EventType(DECLARATION)-round-trip"
      service.recordTimeArgumentCaptor.head._2 shouldBe Duration.between(DeclarationConversationMetric.event.eventStart.zonedDateTime,
                                                                          NotificationConversationMetric.event.eventEnd.zonedDateTime)
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