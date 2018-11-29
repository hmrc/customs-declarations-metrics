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

package uk.gov.hmrc.customs.declarations.metrics.controllers

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, BodyParser}
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.errorBadRequest
import uk.gov.hmrc.customs.api.common.controllers.{ErrorResponse, ResponseContents}
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.declarations.metrics.model.ConversationMetric
import uk.gov.hmrc.customs.declarations.metrics.services.MetricsService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class CustomsDeclarationsMetricsController @Inject() (val logger: CdsLogger,
                                                      metricsService: MetricsService,
                                                      val messagesApi: MessagesApi)
      extends BaseController with HeaderValidator with I18nSupport {

  private val nonJsonBodyErrorMessage = "Request does not contain a valid JSON body"
  private def tryJsonParser: BodyParser[Try[JsValue]] = parse.tolerantText.map(text => Try(Json.parse(text)))

  def post(): Action[Try[JsValue]] = validateHeaders(AcceptHeaderValidation).async(tryJsonParser) {
    implicit request =>

      request.body match {

        case Success(js) =>
          js.validate[ConversationMetric] match {
            case JsSuccess(requestPayload, _) =>
              logger.debug(s"Log-time endpoint called with payload $requestPayload and headers ${request.headers}")
              metricsService.process(requestPayload).map {
                case Right(_) => Accepted
                case Left(errorResult) => errorResult
              }
            case error: JsError =>
              logger.error(s"JSON payload failed schema validation with error $error")
              Future.successful(invalidJsonErrorResponse(error).JsonResult)
          }

        case Failure(ex) =>
          logger.error(s"$nonJsonBodyErrorMessage ${ex.getMessage}")
          Future.successful(errorBadRequest(nonJsonBodyErrorMessage).JsonResult)
      }
  }

  private def invalidJsonErrorResponse(jsError: JsError)(implicit messages: Messages, hc: HeaderCarrier): ErrorResponse = {
    val contents = for {
      (jsPath, validationErrors) <- jsError.errors
      validationError <- validationErrors
      errorMessage = s"$jsPath: ${messages(validationError.message, validationError.args: _*)}"
    } yield ResponseContents("INVALID_JSON", errorMessage)
    logger.error("failed JSON schema validation")

    errorBadRequest("Request failed schema validation").withErrors(contents: _*)
  }

}
