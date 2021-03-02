/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.customs.declarations.metrics.controllers

import play.api.http.{HeaderNames, MimeTypes}
import play.api.mvc._
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.ErrorAcceptHeaderInvalid
import uk.gov.hmrc.customs.api.common.logging.CdsLogger

import scala.concurrent.{ExecutionContext, Future}

trait HeaderValidator extends Results {

  private lazy val validAcceptHeaders = Seq("application/vnd.hmrc.1.0+json", MimeTypes.JSON)

  val AcceptHeaderValidation: Option[String] => Boolean = _ exists (validAcceptHeaders.contains(_))

  protected val logger: CdsLogger

  def validateHeaders(rules: Option[String] => Boolean, controllerComponents: ControllerComponents): ActionBuilder[Request, AnyContent] = new ActionBuilder[Request, AnyContent] {
    def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
      val logMessage = "Received log-time request"
      val headers = request.headers.headers
      logger.debug(s"$logMessage with headers $headers")

      val acceptHeader = HeaderNames.ACCEPT
      val hasAccept = rules(request.headers.get(acceptHeader))

      if (hasAccept) {
        logger.debug(s"$acceptHeader header passed validation from headers $headers")
        block(request)
      } else {
        logger.debug(s"$acceptHeader header failed validation from headers $headers")
        Future.successful(ErrorAcceptHeaderInvalid.JsonResult)
      }
    }

    override def parser: BodyParser[AnyContent] = controllerComponents.parsers.defaultBodyParser

    override protected def executionContext: ExecutionContext = controllerComponents.executionContext
  }
}
