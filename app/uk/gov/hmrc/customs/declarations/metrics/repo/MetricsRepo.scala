package uk.gov.hmrc.customs.declarations.metrics.repo

import javax.inject.Inject

import com.google.inject.ImplementedBy
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.LogTimeRequest
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[MetricsMongoRepo])
trait MetricsRepo {

  def create(logTimeRequest: LogTimeRequest): Future[Boolean]

}


class MetricsMongoRepo @Inject() (mongoDbProvider: MongoDbProvider,
                                  errorHandler: MetricsRepoErrorHandler,
                                  logger: CdsLogger) extends ReactiveRepository[LogTimeRequest, BSONObjectID](
  collectionName = "logTimes",
  mongo = mongoDbProvider.mongo,
  domainFormat = LogTimeRequest.LogTimeRequestJF
) with MetricsRepo {

  private implicit val format = LogTimeRequest.LogTimeRequestJF

  override def create(logTimeRequest: LogTimeRequest): Future[Boolean] = {
    logger.debug(s"saving logTimeRequest: $logTimeRequest")
    lazy val errorMsg = s"Log time request data not inserted for $logTimeRequest"

    collection.insert(logTimeRequest).map {
      writeResult => errorHandler.handleSaveError(writeResult, errorMsg)
    }
  }

}
