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

package uk.gov.hmrc.customs.declarations.metrics.model

import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{ZoneId, ZonedDateTime}
import scala.util.Try

object ModelsZonedDateTimeFormat {

  lazy val dateTimeRequestReads: Reads[ZonedDateTime] = JsPath.read[String].map { zonedDateTime =>
    ZonedDateTime.parse(zonedDateTime)
  }

  lazy val mongoZonedDateTimeFormat: Format[ZonedDateTime] =
    Format(ModelsZonedDateTimeFormat.mongoZonedDateTimeReads, ModelsZonedDateTimeFormat.mongoZonedDateTimeWrites)

  val mongoZonedDateTimeReads: Reads[ZonedDateTime] = (json: JsValue) => {
    Try(json.validate[ZonedDateTime](MongoJavatimeFormats.instantReads.map { instant =>
      ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"))
    }))
      .getOrElse(JsError())
  }

  val mongoZonedDateTimeWrites: Writes[ZonedDateTime] =
    MongoJavatimeFormats.instantWrites.contramap(_.withZoneSameInstant(ZoneId.of("UTC")).toInstant)

}
