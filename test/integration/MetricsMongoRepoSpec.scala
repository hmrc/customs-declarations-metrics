/*
 * Copyright 2021 HM Revenue & Customs
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

package integration

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.libs.json.Json
import play.api.test.Helpers
import reactivemongo.api.DB
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.{ConversationMetrics, MetricsConfig}
import uk.gov.hmrc.customs.declarations.metrics.repo.{MetricsMongoRepo, MetricsRepoErrorHandler, MongoDbProvider}
import uk.gov.hmrc.mongo.MongoSpecSupport
import util.TestData._
import util.UnitSpec

class MetricsMongoRepoSpec extends UnitSpec
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with MockitoSugar
  with MongoSpecSupport {
  self =>

  private val mockLogger = mock[CdsLogger]
  private val mockErrorHandler = mock[MetricsRepoErrorHandler]
  private val mockMetricsConfig = mock[MetricsConfig]
  private implicit val ec = Helpers.stubControllerComponents().executionContext

  val twoWeeksInSeconds = 1209600

  private val mongoDbProvider: MongoDbProvider = new MongoDbProvider {
    override val mongo: () => DB = self.mongo
  }

  private val repository = new MetricsMongoRepo(mongoDbProvider, mockErrorHandler, mockLogger, mockMetricsConfig)

  override def beforeEach() {
    await(repository.drop)
    Mockito.reset(mockErrorHandler, mockLogger)
    when(mockMetricsConfig.ttlInSeconds).thenReturn(twoWeeksInSeconds)
  }

  override def afterAll() {
    await(repository.drop)
  }

  private def collectionSize: Int = {
    await(repository.count(Json.obj()))
  }

  "repository" should {

    "successfully save a metric with a single declaration event" in {
      when(mockErrorHandler.handleSaveError(any(), any())).thenReturn(true)
      val saveResult = await(repository.save(ConversationMetricsWithDeclarationEventOnly))
      saveResult shouldBe true
      collectionSize shouldBe 1

      val findResult = fetchMetrics
      findResult.conversationId.id.toString shouldBe "dff783d7-44ee-4836-93d0-3242da7c225f"
      findResult.events.head.eventType.eventTypeString shouldBe "DECLARATION"
      findResult.events.head.eventStart should not be None
      findResult.events.head.eventEnd should not be None
    }

    "successfully update existing metric with a notification event" in {
      when(mockErrorHandler.handleSaveError(any(), any())).thenReturn(true)
      val saveResult = await(repository.save(ConversationMetricsWithDeclarationEventOnly))
      saveResult shouldBe true
      collectionSize shouldBe 1

      await(repository.updateWithFirstNotification(NotificationConversationMetric))
      collectionSize shouldBe 1

      val findResult = fetchMetrics
      findResult.events.size shouldBe 2
      findResult.conversationId.id.toString shouldBe "dff783d7-44ee-4836-93d0-3242da7c225f"
      findResult.events.head.eventType.eventTypeString shouldBe "DECLARATION"
      findResult.events(1).eventType.eventTypeString shouldBe "NOTIFICATION"
    }

    "no update for notification when declaration not found" in {
      when(mockErrorHandler.handleSaveError(any(), any())).thenReturn(true)
      val caught = intercept[IllegalStateException](await(repository.updateWithFirstNotification(NotificationConversationMetric)))

      caught.getMessage shouldBe "event data not updated for ConversationMetric(dff783d7-44ee-4836-93d0-3242da7c225f,Event(EventType(NOTIFICATION),2014-10-23T00:36:14.123Z,2014-10-23T00:36:18.123Z))"
      collectionSize shouldBe 0
    }

    "no update when notification metric already present" in {
      when(mockErrorHandler.handleSaveError(any(), any())).thenReturn(true)
      await(repository.save(ConversationMetricsWithDeclarationEventOnly))
      await(repository.updateWithFirstNotification(NotificationConversationMetric))

      val caught = intercept[IllegalStateException](await(repository.updateWithFirstNotification(NotificationConversationMetric)))
      caught.getMessage shouldBe "event data not updated for ConversationMetric(dff783d7-44ee-4836-93d0-3242da7c225f,Event(EventType(NOTIFICATION),2014-10-23T00:36:14.123Z,2014-10-23T00:36:18.123Z))"
    }

    "no update when second notification stored" in {
      when(mockErrorHandler.handleSaveError(any(), any())).thenReturn(true)
      await(repository.save(ConversationMetricsWithDeclarationEventOnly))
      await(repository.updateWithFirstNotification(NotificationConversationMetric))

      val caught = intercept[IllegalStateException](await(repository.updateWithFirstNotification(NotificationConversationMetric)))
      caught.getMessage shouldBe "event data not updated for ConversationMetric(dff783d7-44ee-4836-93d0-3242da7c225f,Event(EventType(NOTIFICATION),2014-10-23T00:36:14.123Z,2014-10-23T00:36:18.123Z))"
      collectionSize shouldBe 1
      fetchMetrics.events.size shouldBe 2
    }

    "successfully delete all metrics" in  {
      when(mockErrorHandler.handleSaveError(any(), any())).thenReturn(true)
      await(repository.save(ConversationMetricsWithDeclarationEventOnly))
      await(repository.updateWithFirstNotification(NotificationConversationMetric))
      await(repository.deleteAll())
      collectionSize shouldBe 0
    }

  }

  private def fetchMetrics: ConversationMetrics = await(repository.find("conversationId" -> DeclarationConversationId.id).head)
}
