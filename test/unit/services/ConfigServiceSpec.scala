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

package unit.services

import com.typesafe.config.{Config, ConfigFactory}
import play.api.Configuration
import uk.gov.hmrc.customs.declarations.metrics.common.config.ConfigValidatedNelAdaptor
import uk.gov.hmrc.customs.declarations.metrics.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.MetricsConfig
import uk.gov.hmrc.customs.declarations.metrics.services.ConfigService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import util.UnitSpec

class ConfigServiceSpec extends UnitSpec {

  private val validAppConfig: Config = ConfigFactory.parseString(
    s"""
      |{
      |ttlInSeconds = 101
      |}
    """.stripMargin)

  private val emptyAppConfig: Config = ConfigFactory.parseString("")

  private def testServicesConfig(configuration: Configuration) = new ServicesConfig(configuration)

  private val validServicesConfig = new Configuration(validAppConfig)
  private val emptyServicesConfig = new Configuration(emptyAppConfig)

  private val mockCdsLogger = mock[CdsLogger]

  "ConfigService" should {
    "return config as object model when configuration is valid" in {
      val actual: MetricsConfig = configService(validServicesConfig)

      actual.ttlInSeconds shouldBe 101
    }

    "throw an exception when configuration is invalid, that contains AGGREGATED error messages" in {
      val expected =
        """
          |Could not find config key 'ttlInSeconds'""".stripMargin

      val caught = intercept[IllegalStateException]{ configService(emptyServicesConfig) }

      caught.getMessage shouldBe expected
    }
  }

  private def configService(conf: Configuration) =
    new ConfigService(new ConfigValidatedNelAdaptor(testServicesConfig(conf), conf), mockCdsLogger)

}
