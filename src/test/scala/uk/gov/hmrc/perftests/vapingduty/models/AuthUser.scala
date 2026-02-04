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

  private val vpdEnrolmentKey          = "HMRC-VPD-ORG"
  private val vpdIdentifierName        = "ZVPD"
  private val activatedState           = "Activated"
  private val vpdIdentifierValue       = "X"
  val contactPreferencePostIdentifier  = "XMADP1000100211"
  val contactPreferenceEmailIdentifier = "XMADP0000100211"

  def organisation(enrolled: Boolean = false, identifierValue: String = vpdIdentifierValue): AuthUser =
    if (enrolled)
      AuthUser("Organisation", activatedState, vpdEnrolmentKey, vpdIdentifierName, identifierValue)
    else
      AuthUser("Organisation")

  def agent(): AuthUser =
    AuthUser("Agent")

  def individual(): AuthUser =
    AuthUser("Individual")
}
