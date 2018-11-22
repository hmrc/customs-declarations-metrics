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

package uk.gov.hmrc.customs.declarations.metrics.services

import javax.inject.Inject

import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.EventTime
import uk.gov.hmrc.customs.declarations.metrics.repo.MetricsRepo

import scala.concurrent.Future

class MetricsService @Inject()(cdsLogger: CdsLogger, metricsRepo: MetricsRepo) {

  def process(eventTime: EventTime): Future[Boolean] = {
    metricsRepo.save(eventTime)

    //switch on EventType

    //dec_start => store graphite count & digital elapsed time

    //cn => find (update?) mongo rec & calc elapsed time & store count & elapsed time
  }

}
