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

package unit.services


import com.codahale.metrics.MetricRegistry
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{RETURNS_DEEP_STUBS, verify, when}
import play.api.test.Helpers
import uk.gov.hmrc.customs.declarations.metrics.common.controllers.ErrorResponse.ErrorInternalServerError
import uk.gov.hmrc.customs.declarations.metrics.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.{ConversationMetric, ConversationMetrics}
import uk.gov.hmrc.customs.declarations.metrics.repo.MetricsRepo
import uk.gov.hmrc.customs.declarations.metrics.services.{HasMetrics, MetricsService}
import uk.gov.hmrc.play.bootstrap.metrics.Metrics
import util.TestData.{ConversationMetrics1, DeclarationConversationMetric, NotificationConversationMetric}
import util.UnitSpec

import java.time.Duration
import scala.concurrent.{ExecutionContext, Future}

class MetricsServiceSpec extends UnitSpec {

  trait FakeHasMetrics extends HasMetrics{
   var recordTimeArgumentCaptor: Map[String, Duration] = scala.collection.immutable.Map[String, Duration]()
    override lazy val registry: MetricRegistry = mock[MetricRegistry]
    override def recordTime(timerName: Metric, duration: Duration): Unit = {
      recordTimeArgumentCaptor = recordTimeArgumentCaptor + (timerName -> duration)
    }
  }

  trait SetUp {
    private implicit val ec: ExecutionContext = Helpers.stubControllerComponents().executionContext
    val mockMetrics: Metrics = mock[Metrics]
    val mockLogger: CdsLogger = mock[CdsLogger]
    val mockRepo: MetricsRepo = mock[MetricsRepo]
    val mockMetricsRegistry: MetricRegistry = mock[MetricRegistry](RETURNS_DEEP_STUBS)

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
      await(result) shouldBe Left(ErrorInternalServerError.JsonResult)
    }

    "save a notification metric successfully and record 3 metrics" in new SetUp() {
      when(mockRepo.updateWithFirstNotification(any[ConversationMetric])).thenReturn(Future.successful(ConversationMetrics1))

      val result = await(service.process(NotificationConversationMetric))
      service.recordTimeArgumentCaptor.size shouldBe 3
      service.recordTimeArgumentCaptor("declaration-round-trip").toMillis shouldBe 64000
      service.recordTimeArgumentCaptor("notification-digital").toMillis shouldBe 4000
      service.recordTimeArgumentCaptor("declaration-digital-total").toMillis shouldBe 6000

      verify(mockRepo).updateWithFirstNotification(any[ConversationMetric])
      result shouldBe Right(())
    }

    "save a notification metric throws an Exception when update fails " in new SetUp() {
      when(mockRepo.updateWithFirstNotification(any[ConversationMetric])).thenThrow(new IllegalStateException("Some Error"))

      val caught: IllegalStateException = intercept[IllegalStateException](await(service.process(NotificationConversationMetric)))

      verify(mockRepo).updateWithFirstNotification(any[ConversationMetric])
      caught.getMessage shouldBe "Some Error"
    }

  }


}
