/*
 * Copyright 2019 HM Revenue & Customs
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

import java.time.Duration

import com.kenshoo.play.metrics.Metrics
import java.util.concurrent.TimeUnit.NANOSECONDS

import com.codahale.metrics.MetricRegistry
import uk.gov.hmrc.customs.api.common.logging.CdsLogger

trait HasMetrics {
  
  type Metric = String
  def metrics: Metrics
  val logger: CdsLogger

  lazy val registry: MetricRegistry = metrics.defaultRegistry

  def recordTime(metric: Metric, duration: Duration): Unit = {

    val nanos = duration.toNanos
    logger.debug(s"recording $nanos ns for $metric")
    registry.getTimers
      .getOrDefault(metric, registry.timer(metric))
      .update(nanos, NANOSECONDS)

    registry.counter(s"$metric-counter").inc()
  }

}
