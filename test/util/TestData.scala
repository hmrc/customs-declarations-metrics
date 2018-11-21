package util

import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.{ACCEPT, CONTENT_TYPE}

import scala.util.Try

object TestData {


val ValidJson = Json.parse("""
       |{
       | "EventType": "DEC-ENTRY"
       |}
    """.stripMargin)


  val ValidRequest: FakeRequest[AnyContentAsJson] = FakeRequest("POST","/log-time")
    .withHeaders(RequestHeaders.ValidHeaders.toSeq: _*)
    .withJsonBody(ValidJson)

  val ValidRequestAsTryJsValue: Request[Try[JsValue]] = ValidRequest.copyFakeRequest[Try[JsValue]](body = Try(ValidRequest.body.json))
}


object RequestHeaders {

  val CONTENT_TYPE_HEADER: (String, String) = CONTENT_TYPE -> MimeTypes.JSON

  val CONTENT_TYPE_HEADER_INVALID: (String, String) = CONTENT_TYPE -> MimeTypes.XML

  val ACCEPT_HMRC_HEADER: (String, String) = ACCEPT -> "application/vnd.hmrc.1.0+json"

  val ACCEPT_HEADER: (String, String) = ACCEPT -> MimeTypes.JSON

  val ACCEPT_HEADER_INVALID: (String, String) = ACCEPT -> MimeTypes.XML

  val ValidHeaders = Map(
    CONTENT_TYPE_HEADER,
    ACCEPT_HMRC_HEADER)
}