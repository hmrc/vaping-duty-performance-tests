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

import java.security.SecureRandom

final case class AuthUser(
  affinityGroup: String,
  enrolmentState: String = "",
  enrolmentKey: String = "",
  taxIdentifierName: String = "",
  taxIdentifierValue: String = ""
)

object AuthUser {

  private val secureRandom      = new SecureRandom()
  private val vpdEnrolmentKey   = "HMRC-VPD-ORG"
  private val vpdIdentifierName = "ZVPD"
  private val activatedState    = "Activated"

  private def randomVpdId(
    prefix: String = "XI",
    emailFlag: String = "0",
    suffix: String = "200"
  ): String = {
    val offFlags = (1 to 3).map(_ => secureRandom.nextInt(10)).mkString
    s"${prefix}WK$emailFlag$offFlags${suffix}WK"
  }

  val contactPreferencePostToPost: AuthUser =
    organisation(enrolled = true, identifierValue = randomVpdId(emailFlag = "1"))

  def randomOrganisation(): AuthUser =
    organisation(enrolled = true, identifierValue = randomVpdId(emailFlag = "5"))

  def organisation(enrolled: Boolean = false, identifierValue: String = "X"): AuthUser =
    if (enrolled)
      AuthUser("Organisation", activatedState, vpdEnrolmentKey, vpdIdentifierName, identifierValue)
    else
      AuthUser("Organisation")

  def agent(): AuthUser =
    AuthUser("Agent")

  def individual(): AuthUser =
    AuthUser("Individual")
}
