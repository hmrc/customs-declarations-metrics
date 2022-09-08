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

package uk.gov.hmrc.customs.declarations.metrics.repo

import com.google.inject.ImplementedBy
import org.mongodb.scala.Document
import org.mongodb.scala.model.Updates.push
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Format, OFormat}
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.{ConversationMetric, ConversationMetrics, Event, MetricsConfig}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[MetricsMongoRepo])
trait MetricsRepo {

  def save(conversationMetrics: ConversationMetrics): Future[Boolean]

  def updateWithFirstNotification(conversationMetric: ConversationMetric): Future[ConversationMetrics]

  def deleteAll(): Future[Unit]
}

@Singleton
class MetricsMongoRepo @Inject()(mongo: MongoComponent,
                                 logger: CdsLogger,
                                 metricsConfig: MetricsConfig)(implicit ec: ExecutionContext) extends PlayMongoRepository[ConversationMetrics](
  collectionName = "metrics",
  mongoComponent = mongo,
  domainFormat = ConversationMetrics.conversationMetricsJF,
  indexes = Seq(
    IndexModel(
      Indexes.ascending("conversationId"),
      IndexOptions()
        .name("conversationId-Index")
        .unique(true)
    ),
    IndexModel(
      Indexes.descending("createdAt"),
      IndexOptions()
        .name("createdDate-Index")
        .unique(false)
        .expireAfter(metricsConfig.ttlInSeconds, java.util.concurrent.TimeUnit.SECONDS)

    )
  )
) with MetricsRepo {

  private implicit val format: OFormat[ConversationMetrics] = ConversationMetrics.conversationMetricsJF
  private implicit val formatEvent: Format[Event] = Event.EventJF

  private val ttlIndexName = "createdDate-Index"
  private val ttlInSeconds = metricsConfig.ttlInSeconds
    private val ttlIndex = IndexModel(
      Indexes.descending("createdAt"),
      IndexOptions()
        .name(ttlIndexName)
        .unique(false)
        .expireAfter(ttlInSeconds, java.util.concurrent.TimeUnit.SECONDS)
    )


//    dropInvalidIndexes.flatMap { _ =>
////      collection.indexesManager.ensure(ttlIndex)
//
//      collection.listIndexes().toFuture().
//    }


  override def save(conversationMetrics: ConversationMetrics): Future[Boolean] = {
    logger.debug(s"saving conversationMetrics: $conversationMetrics")
    lazy val errorMsg = s"event data not inserted for $conversationMetrics"

    collection.insertOne(conversationMetrics).toFuture().map(result => result.wasAcknowledged()).recover {

      val errorMsg1 = s"$errorMsg"
      logger.error(errorMsg1)
      throw new RuntimeException(errorMsg1)
      //              throw new IllegalStateException(errorMsg1)
    }
  }


  override def updateWithFirstNotification(conversationMetric: ConversationMetric): Future[ConversationMetrics] = {
    logger.debug(s"updating with first notification: $conversationMetric")
    lazy val errorMsg = s"event data not updated for $conversationMetric"

    val selector = and(equal("conversationId",conversationMetric.conversationId.id),exists("events.1",exists = false))

    val update =  push("events", conversationMetric.event)

//    val result: Future[ConversationMetrics] = findAndUpdate(selector, update, fetchNewObject = true).map { result =>

    val result= collection.findOneAndUpdate(filter = selector, update = update).toFuture()
//      .map{result =>
//      if (result.lastError.isDefined && result.lastError.get.err.isDefined) {
//        logger.error(s"mongo error: ${result.lastError.get.err.get}")
//        throw new IllegalStateException(errorMsg)
//      } else {
//        result.result[ConversationMetrics].getOrElse({
//          logger.debug(errorMsg)
//          throw new IllegalStateException(errorMsg)
//        })
//      }
//    }

   result.recover {
     logger.error(s"mongo error: findOneAndUpdate failed")
     throw new IllegalStateException(errorMsg)
   }

  }

  private def dropInvalidIndexes: Future[_] =
    collection.listIndexes.toFuture.map( indexes =>
      indexes
        .find { index =>
          index.get("name").contains(ttlIndexName) &&
            !index.get("expireAfterSeconds").contains(ttlInSeconds)
        }
        .map { _ =>
          logger.debug(s"dropping $ttlIndexName index as ttl value is incorrect")
          collection.dropIndex(ttlIndexName)
        }
        .getOrElse(Future.successful(())))


  override def deleteAll(): Future[Unit] = {
    logger.debug(s"deleting all metrics")

    collection.deleteMany(filter = Document()).toFuture().map { result =>
      logger.debug(s"deleted ${result.getDeletedCount} metrics")
    }
  }

}
