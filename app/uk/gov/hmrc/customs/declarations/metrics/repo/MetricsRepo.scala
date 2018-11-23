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

package uk.gov.hmrc.customs.declarations.metrics.repo

import javax.inject.Inject

import com.google.inject.ImplementedBy
import play.api.libs.json.Json
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.JsObjectDocumentWriter
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.{ConversationMetric, ConversationMetrics, Event}
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@ImplementedBy(classOf[MetricsMongoRepo])
trait MetricsRepo {

  def save(conversationMetrics: ConversationMetrics): Future[Boolean]
  def updateWithFirstNotification(conversationMetric: ConversationMetric): Future[ConversationMetrics]
}

class MetricsMongoRepo @Inject() (mongoDbProvider: MongoDbProvider,
                                  errorHandler: MetricsRepoErrorHandler,
                                  logger: CdsLogger) extends ReactiveRepository[ConversationMetrics, BSONObjectID](
  collectionName = "logTimes",
  mongo = mongoDbProvider.mongo,
  domainFormat = ConversationMetrics.conversationMetricsJF
) with MetricsRepo {

  private implicit val format = ConversationMetrics.conversationMetricsJF
  private implicit val formatEvent = Event.EventJF

  override def indexes: Seq[Index] = Seq(
    Index(
      //TODO check IndexType
      key = Seq("conversationId" -> IndexType.Ascending),
      name = Some("conversationId-Index"),
      unique = true
    )
  )

  override def save(conversationMetrics: ConversationMetrics): Future[Boolean] = {
    logger.debug(s"saving conversationMetrics: $conversationMetrics")
    lazy val errorMsg = s"event data not inserted for $conversationMetrics"

    collection.insert(conversationMetrics).map {
      writeResult => errorHandler.handleSaveError(writeResult, errorMsg)
    }
  }

  override def updateWithFirstNotification(conversationMetric: ConversationMetric): Future[ConversationMetrics] = {
    logger.debug(s"updating with first notification: $conversationMetric")
    lazy val errorMsg = s"event data not inserted for $conversationMetric"

    //TODO add criteria to selector so that update is only done when metric contains Declaration event only
    val selector = Json.obj("conversationId" -> conversationMetric.conversationId.id)
    val update = Json.obj("$addToSet" -> Json.obj("events" -> conversationMetric.event))

    val result: Future[ConversationMetrics] = collection.findAndUpdate(selector, update).map { result =>
      result.result[ConversationMetrics].getOrElse(throw new IllegalStateException(errorMsg))
    }
    result
  }

}
