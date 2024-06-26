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

package uk.gov.hmrc.customs.declarations.metrics.common

import cats.data.ValidatedNel

package object config {

  /*
  we need this type alias which has only one parameterised type (ie fixes String) so that we can use the `mapN`
  function in client code (note you will need import cats.implicits._)
  */
  type CustomsValidatedNel[A] = ValidatedNel[String, A]
}
