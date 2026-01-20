/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.perftests.vapingduty.models

final case class AuthUser(
  affinityGroup: String,
  enrolmentState: String = "",
  enrolmentKey: String = "",
  taxIdentifierName: String = "",
  taxIdentifierValue: String = ""
)
object AuthUser {

  private val VpdEnrolmentKey    = "HMRC-VPD-ORG"
  private val VpdIdentifierName  = "ZVPD"
  private val VpdIdentifierValue = "X"
  private val ActivatedState     = "Activated"

  def organisation(enrolled: Boolean = false): AuthUser =
    if (enrolled)
      AuthUser("Organisation", ActivatedState, VpdEnrolmentKey, VpdIdentifierName, VpdIdentifierValue)
    else
      AuthUser("Organisation")

  def agent(): AuthUser =
    AuthUser("Agent")

  def individual(): AuthUser =
    AuthUser("Individual")
}
