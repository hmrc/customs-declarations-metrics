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

package integration

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.libs.json.Json
import reactivemongo.api.DB
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.{ConversationId, LogTimeRequest}
import uk.gov.hmrc.customs.declarations.metrics.repo.{MetricsMongoRepo, MetricsRepoErrorHandler, MongoDbProvider}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec
import util.MockitoPassByNameHelper.PassByNameVerifier
import util.TestData.{ConversationId1, LogTimeRequest1}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class MetricsMongoRepoSpec extends UnitSpec
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with MockitoSugar
  with MongoSpecSupport  { self =>

  private val mockLogger = mock[CdsLogger]
  private val mockErrorHandler = mock[MetricsRepoErrorHandler]

  private val mongoDbProvider = new MongoDbProvider {
    override val mongo: () => DB = self.mongo
  }

  private val repository = new MetricsMongoRepo(mongoDbProvider, mockErrorHandler, mockLogger)
  private val repositoryWithOneMaxRecord = new MetricsMongoRepo(mongoDbProvider, mockErrorHandler, mockLogger)

  override def beforeEach() {
    await(repository.drop)
    Mockito.reset(mockErrorHandler, mockLogger)
  }

  override def afterAll() {
    await(repository.drop)
  }

  private def collectionSize: Int = {
    await(repository.collection.count())
  }

  private def selector(conversationId: ConversationId) = {
    Json.obj("conversationId" -> conversationId.id)
  }

  private def logVerifier(logLevel: String, logText: String) = {
    PassByNameVerifier(mockLogger, logLevel)
      .withByNameParam(logText)
      .withParamMatcher(any[HeaderCarrier])
      .verify()
  }

  "repository" should {
    "successfully save a single notification" in {
      when(mockErrorHandler.handleSaveError(any(), any())).thenReturn(true)
      val saveResult = await(repository.create(LogTimeRequest1))
      saveResult shouldBe true
      collectionSize shouldBe 1

      val findResult = await(repository.collection.find(selector(ConversationId1)).one[LogTimeRequest]).get
      findResult.conversationId should not be None
      findResult.eventType should not be None
    }

//    "successfully save when called multiple times" in {
//      await(repository.save(client1Notification1))
//      await(repository.save(client1Notification2))
//      await(repository.save(client2Notification1))
//
//      collectionSize shouldBe 3
//      val clientNotifications = await(repository.collection.find(selector(validClientSubscriptionId1)).cursor[ClientNotification]().collect[List](Int.MaxValue, Cursor.FailOnError[List[ClientNotification]]()))
//      clientNotifications.size shouldBe 2
//      clientNotifications.head.id should not be None
//    }

//    "fetch by clientSubscriptionId should return a two records when not limited by max records to fetch" in {
//      await(repository.save(client1Notification1))
//      await(repository.save(client1Notification2))
//      await(repository.save(client2Notification1))
//
//      val clientNotifications = await(repository.fetch(validClientSubscriptionId1))
//
//      clientNotifications.size shouldBe 2
//      clientNotifications.head.notification shouldBe client1Notification1.notification
//      clientNotifications(1).notification shouldBe client1Notification2.notification
//
//      logVerifier("debug", "fetching clientNotification(s) with csid: eaca01f9-ec3b-4ede-b263-61b626dde232 and with max records=5")
//    }
//
//    "fetch by clientSubscriptionId should return a one record when limited by one max record to fetch" in {
//      await(repository.save(client1Notification1))
//      await(repository.save(client1Notification2))
//      await(repository.save(client2Notification1))
//
//      val clientNotifications = await(repositoryWithOneMaxRecord.fetch(validClientSubscriptionId1))
//
//      clientNotifications.size shouldBe 1
//      clientNotifications.head.notification shouldBe client1Notification1.notification
//    }

//    "return empty List when not found" in {
//      await(repository.save(client1Notification1))
//      await(repository.save(client1Notification2))
//      val nonExistentClientNotification = client2Notification1
//
//      await(repository.fetch(nonExistentClientNotification.csid)) shouldBe Nil
//    }

  }
}