/*
 * Copyright 2022 HM Revenue & Customs
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

import org.mockito.Mockito
import org.mockito.Mockito._
import org.mongodb.scala.model.Filters
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.{ConversationMetrics, MetricsConfig}

import uk.gov.hmrc.customs.declarations.metrics.repo.MetricsMongoRepo
import uk.gov.hmrc.mongo.play.json.formats.MongoUuidFormats
import util.TestData._
import util.UnitSpec
import scala.concurrent.ExecutionContext

class MetricsMongoRepoSpec extends UnitSpec
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with GuiceOneAppPerSuite
  with MockitoSugar
  with MongoUuidFormats.Implicits {

  implicit val ec: ExecutionContext = Helpers.stubControllerComponents().executionContext

  private val mockLogger = mock[CdsLogger]
  private val mockMetricsConfig = mock[MetricsConfig]

  val twoWeeksInSeconds = 1209600
    lazy val repository: MetricsMongoRepo = app.injector.instanceOf[MetricsMongoRepo]


  override def beforeEach() {
    await(repository.collection.drop.toFuture())
    Mockito.reset(mockLogger)
    when(mockMetricsConfig.ttlInSeconds).thenReturn(twoWeeksInSeconds)
  }

  override def afterAll() {
    await(repository.collection.drop.toFuture())
  }

  private def collectionSize: Int = {
    await(repository.collection.countDocuments().toFuture()).toInt
  }

  "repository" should {

    "successfully save a metric with a single declaration event" in {
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
      val caught = intercept[IllegalStateException](await(repository.updateWithFirstNotification(NotificationConversationMetric)))

      caught.getMessage shouldBe "event data not updated for ConversationMetric(dff783d7-44ee-4836-93d0-3242da7c225f,Event(EventType(NOTIFICATION),2014-10-23T00:36:14.123Z,2014-10-23T00:36:18.123Z))"
      collectionSize shouldBe 0
    }

    "no update when notification metric already present" in {
      await(repository.save(ConversationMetricsWithDeclarationEventOnly))
      await(repository.updateWithFirstNotification(NotificationConversationMetric))

      val caught = intercept[IllegalStateException](await(repository.updateWithFirstNotification(NotificationConversationMetric)))
      caught.getMessage shouldBe "event data not updated for ConversationMetric(dff783d7-44ee-4836-93d0-3242da7c225f,Event(EventType(NOTIFICATION),2014-10-23T00:36:14.123Z,2014-10-23T00:36:18.123Z))"
    }

    "no update when second notification stored" in {
      await(repository.save(ConversationMetricsWithDeclarationEventOnly))
      await(repository.updateWithFirstNotification(NotificationConversationMetric))

      val caught = intercept[IllegalStateException](await(repository.updateWithFirstNotification(NotificationConversationMetric)))
      caught.getMessage shouldBe "event data not updated for ConversationMetric(dff783d7-44ee-4836-93d0-3242da7c225f,Event(EventType(NOTIFICATION),2014-10-23T00:36:14.123Z,2014-10-23T00:36:18.123Z))"
      collectionSize shouldBe 1
      fetchMetrics.events.size shouldBe 2
    }

    "successfully delete all metrics" in {
      await(repository.save(ConversationMetricsWithDeclarationEventOnly))
      await(repository.updateWithFirstNotification(NotificationConversationMetric))
      await(repository.deleteAll())
      collectionSize shouldBe 0
    }

  }

  private def fetchMetrics: ConversationMetrics = await(repository.collection.find(filter = Filters.equal("conversationId", DeclarationConversationId.id.toString)).toFuture()).head
}
