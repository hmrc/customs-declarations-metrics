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
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.EventTime
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[MetricsMongoRepo])
trait MetricsRepo {

  def save(eventTime: EventTime): Future[Boolean]

}


class MetricsMongoRepo @Inject() (mongoDbProvider: MongoDbProvider,
                                  errorHandler: MetricsRepoErrorHandler,
                                  logger: CdsLogger) extends ReactiveRepository[EventTime, BSONObjectID](
  collectionName = "logTimes",
  mongo = mongoDbProvider.mongo,
  domainFormat = EventTime.EventTimeJF
) with MetricsRepo {

  private implicit val format = EventTime.EventTimeJF

  override def indexes: Seq[Index] = Seq(
    Index(
      //TODO check IndexType
      key = Seq("conversationId" -> IndexType.Ascending),
      name = Some("conversationId-Index"),
      unique = true
    )
  )


  override def save(eventTime: EventTime): Future[Boolean] = {
    logger.debug(s"saving eventTime: $eventTime")
    lazy val errorMsg = s"Log time request data not inserted for $eventTime"

    collection.insert(eventTime).map {
      writeResult => errorHandler.handleSaveError(writeResult, errorMsg)
    }
  }

}
