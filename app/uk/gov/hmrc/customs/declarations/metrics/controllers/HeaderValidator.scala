package uk.gov.hmrc.customs.declarations.metrics.controllers

import play.api.http.{HeaderNames, MimeTypes}
import play.api.mvc.{ActionBuilder, Request, Result, Results}
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.ErrorAcceptHeaderInvalid

import scala.concurrent.Future

trait HeaderValidator extends Results {

  private lazy val validAcceptHeaders = Seq("application/vnd.hmrc.1.0+json", MimeTypes.JSON)

  val acceptHeaderValidation: (Option[String] => Boolean) = _ exists (validAcceptHeaders.contains(_))

  //val notificationLogger: NotificationLogger

  def validateAccept(rules: Option[String] => Boolean): ActionBuilder[Request] = new ActionBuilder[Request] {
    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
      //val logMessage = "Received notification"
      val headers = request.headers.headers
      //notificationLogger.debug(logMessage, headers)

      val acceptHeader = HeaderNames.ACCEPT
      val hasAccept = rules(request.headers.get(acceptHeader))

      if (hasAccept) {
        //notificationLogger.debug(s"$acceptHeader header passed validation", headers)
        block(request)
      } else {
       // notificationLogger.debug(s"$acceptHeader header failed validation", headers)
        Future.successful(ErrorAcceptHeaderInvalid.JsonResult)
      }
    }
  }
}