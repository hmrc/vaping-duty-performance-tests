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

package uk.gov.hmrc.perftests.vapingduty

import io.gatling.core.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.regex.RegexCheckType
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmrc.performance.conf.ServicesConfiguration

object VapingDutyRequests extends ServicesConfiguration {

  val baseUrl: String = baseUrlFor("vaping-duty-frontend")
  val route: String   = "/vaping-duty"
  val CsrfPattern     = """<input type="hidden" name="csrfToken" value="([^"]+)""""
  val authUrl: String = baseUrlFor("auth-login-stub")

  def saveCsrfToken(): CheckBuilder[RegexCheckType, String] = regex(_ => CsrfPattern).saveAs("csrfToken")

  val getAuthLoginPage: HttpRequestBuilder =
    http("Navigate to auth login stub page")
      .get(s"$authUrl/auth-login-stub/gg-sign-in": String)
      .check(status.is(200))
      .check(saveCsrfToken())

  def postAuthLoginPage(enrolmentName: String = "VPPAID", affinityGroup: String = "Organisation"): HttpRequestBuilder =
    http("Login with user credentials")
      .post(s"$authUrl/auth-login-stub/gg-sign-in")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("credentialStrength", "strong")
      .formParam("confidenceLevel", "50")
      .formParam("authorityId", "")
      .formParam("groupIdentifier", "")
      .formParam("email", "user@test.com")
      .formParam("credentialRole", "User")
      .formParam("affinityGroup", affinityGroup)
      .formParam("enrolment[0].state", "Activated")
      .formParam("enrolment[0].name", "HMRC-VPD-ORG")
      .formParam("enrolment[0].taxIdentifier[0].name", enrolmentName)
      .formParam("enrolment[0].taxIdentifier[0].value", "x")
      .formParam("redirectionUrl", s"$baseUrl/$route/vaping-duty-frontend/")
      .check(status.is(303))

  val navigateToVapingDutyPage: HttpRequestBuilder =
    http("Navigate to vaping duty Page")
      .get(s"$baseUrl$route")
      .check(status.is(200))

  val GetEnrolmentApprovalPage: HttpRequestBuilder =
    http("Get Enrolment Approval Page")
      .get(s"$baseUrl$route/enrolment/approval-id")
      .check(status.is(200))
      .check(saveCsrfToken())

  def PostEnrolmentApprovalPage(enrolmentApprovalQuestion: Boolean): HttpRequestBuilder =
    http("Post Enrolment Approval Page")
      .post(s"$baseUrl$route/enrolment/approval-id")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", enrolmentApprovalQuestion)
      .check(status.is(303))

  val GetEnrolmentOrganisationSignInPage: HttpRequestBuilder =
    http("Get Enrolment Organisation Sign In Page")
      .get(s"$baseUrl$route/enrolment/organisation-sign-in")
      .check(status.is(200))

  val GetVPDIDApprovalRequiredPage: HttpRequestBuilder =
    http("Get VPDID Approval Required Page")
      .get(s"$baseUrl$route/enrolment/no-approval-id")
      .check(status.is(200))
}
