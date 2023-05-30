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

package uk.gov.hmrc.customs.declarations.metrics.repo

import com.google.inject.ImplementedBy
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates.push
import org.mongodb.scala.model._
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.{ConversationMetric, ConversationMetrics}
import uk.gov.hmrc.customs.declarations.metrics.model.MetricsConfig
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
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
  replaceIndexes = metricsConfig.replaceIndexes,
  indexes = Seq(
    IndexModel(
      Indexes.ascending("conversationId"),
      IndexOptions()
        .name("conversationId-Index")
        .unique(true)
    ),
    IndexModel(
      Indexes.descending(metricsConfig.createdDateIndex),
      IndexOptions()
        .name("createdDate-Index")
        .unique(false)
        .expireAfter(metricsConfig.ttlInSeconds, TimeUnit.SECONDS)

    )
  )

) with MetricsRepo {

  override def save(conversationMetrics: ConversationMetrics): Future[Boolean] = {
    logger.debug(s"saving conversationMetrics: $conversationMetrics")
    lazy val errorMsg = s"event data not inserted for $conversationMetrics"

    collection.insertOne(conversationMetrics).toFuture().map(result => result.wasAcknowledged())
      .recover {
        case e => val errorMsg1 = s"$errorMsg: ${e.getMessage}"
          logger.error(errorMsg1)
          throw new IllegalStateException(errorMsg1)
      }
  }

  override def updateWithFirstNotification(conversationMetric: ConversationMetric): Future[ConversationMetrics] = {
    logger.debug(s"updating with first notification: $conversationMetric")
    lazy val errorMsg = s"event data not updated for $conversationMetric"

    val selector = and(equal(
      "conversationId", conversationMetric.conversationId.id.toString),
      exists("events.1", exists = false))

    val update = push("events", Codecs.toBson(conversationMetric.event))

    val result = collection.findOneAndUpdate(filter = selector, update = update,
      options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)).toFutureOption()

    result.map {
      case Some(record) => record
      case None         => logger.error(s"mongo error: findOneAndUpdate failed for: ${conversationMetric.conversationId}")
        logger.debug(errorMsg)
        throw new IllegalStateException(errorMsg)
    }
  }

  override def deleteAll(): Future[Unit] = {
    logger.debug(s"deleting all metrics")

    collection.deleteMany(filter = Document()).toFuture().map { result =>
      logger.debug(s"deleted ${result.getDeletedCount} metrics")
    }
  }

}
