package uk.gov.hmrc.customs.declarations.metrics.services

import javax.inject.Inject

import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.LogTimeRequest
import uk.gov.hmrc.customs.declarations.metrics.repo.MetricsRepo

import scala.concurrent.Future

class MetricsService @Inject()(cdsLogger: CdsLogger, metricsRepo: MetricsRepo) {

  def process(logTimeRequest: LogTimeRequest): Future[Boolean] = {
    metricsRepo.create(logTimeRequest)
  }

}
