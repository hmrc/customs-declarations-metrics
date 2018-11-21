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

import javax.inject.Singleton
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc
import play.api.mvc.{Action, AnyContentAsEmpty, BodyParser, Result}
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.errorBadRequest
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CustomsDeclarationsMetricsController extends BaseController with HeaderValidator {

  protected def tryJsonParser: BodyParser[Try[JsValue]] = parse.tolerantText.map(text => Try(Json.parse(text)))

  def post(): Action[Try[JsValue]] = validateAccept(acceptHeaderValidation).async(tryJsonParser) {
    implicit request =>

      request.body match {

        case Success(js) =>
          js.validate[LogTimeRequest] match {
            case JsSuccess(requestPayload, _) =>
             // logger.debug(s"${LoggingHelper.logMsgPrefix(requestPayload.conversationId)} Notification passed header validation with payload containing ", url = requestPayload.url.toString, payload = requestPayload.xmlPayload)
              //callOutboundService(requestPayload)
              Future.successful(Accepted)
            case error: JsError =>
              //logger.error("JSON payload failed schema validation")
              //Future.successful(invalidJsonErrorResponse(error).JsonResult)
              Future.successful(InternalServerError)
          }

        case Failure(ex) =>
          //logger.error(nonJsonBodyErrorMessage)
          //Future.successful(errorBadRequest(nonJsonBodyErrorMessage).JsonResult)
          Future.successful(InternalServerError)
      }
  }

  def helloWorld: Action[mvc.AnyContent] = Action {
    Ok("Hello World!!")
  }

}
