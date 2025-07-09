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

package uk.gov.hmrc.customs.declarations.metrics.services

import uk.gov.hmrc.customs.declarations.metrics.common.config.ConfigValidatedNelAdaptor
import uk.gov.hmrc.customs.declarations.metrics.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.MetricsConfig

import javax.inject.{Inject, Singleton}

@Singleton
class ConfigService @Inject() (configValidatedNel: ConfigValidatedNelAdaptor, logger: CdsLogger) extends MetricsConfig {

  private case class MetricsConfigImpl(ttlInSeconds: Int) extends MetricsConfig

  private val config = {
    val validatedConfig = configValidatedNel.root.int("ttlInSeconds") map this.MetricsConfigImpl.apply
    validatedConfig.fold({
      nel => // error case exposes nel (a NotEmptyList)
        val errorMsg = "\n" + nel.toList.mkString("\n")
        logger.error(errorMsg)
        throw new IllegalStateException(errorMsg)
    },
      config => config // success case exposes the value class
    )
  }

  override def ttlInSeconds: Int = config.ttlInSeconds
}
