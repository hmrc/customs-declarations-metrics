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

package util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import util.ExternalServicesConfiguration._

trait WireMockRunner {

  lazy val wireMockUrl = s"http://$Host:$Port"
  lazy val wireMockServer = new WireMockServer(wireMockConfig().port(Port))

  def startMockServer(): Unit = {
    if (!wireMockServer.isRunning) wireMockServer.start()
    WireMock.configureFor(Host, Port)
  }

  def resetMockServer(): Unit = {
    WireMock.reset()
  }

  def stopMockServer(): Unit = {
    wireMockServer.stop()
  }

  def withoutWireMockServer(thunk: => Any): Unit = {
    if (wireMockServer.isRunning) {
      stopMockServer()
      try thunk
      finally startMockServer()
    } else {
      thunk
    }
  }

}

object ExternalServicesConfiguration {
  val Port: Int = sys.env.getOrElse("WIREMOCK_SERVICE_PORT", "11111").toInt
  val Host = "localhost"
}
