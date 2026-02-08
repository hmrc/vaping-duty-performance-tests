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
import uk.gov.hmrc.perftests.vapingduty.models.AuthUser

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object VapingDutyRequests extends ServicesConfiguration {

  val baseUrl: String                 = baseUrlFor("vaping-duty-frontend")
  val authUrl: String                 = baseUrlFor("auth-login-stub")
  val emailVerificationUrl: String    = baseUrlFor("email-verification")
  val emailAddressToVerify: String    = randomTestEmail()
  val vapingDutyRoute: String         = "/vaping-duty"
  val enrolmentRoute: String          = "enrolment"
  val contactPreferencesRoute: String = "contact-preferences"
  val CsrfPattern                     = """<input type="hidden" name="csrfToken" value="([^"]+)""""

  private val ggAuthSignInUrl: String        = s"$authUrl/auth-login-stub/gg-sign-in"
  private val authSessionUrl: String         = s"$authUrl/auth-login-stub/session"
  private val doYouHaveApprovalIdUrl: String = s"$baseUrl/$vapingDutyRoute/$enrolmentRoute/do-you-have-an-approval-id"
  private val organisationSignUrl: String    = s"$baseUrl$vapingDutyRoute/$enrolmentRoute/sign-in"
  private val youNeedAnApprovalIDUrl: String = s"$baseUrl$vapingDutyRoute/$enrolmentRoute/you-need-an-approval-id"
  private val alreadyEnrolledUrl: String     = s"$baseUrl$vapingDutyRoute/$enrolmentRoute/already-enrolled"

  val howDoYouWantToBeContactedUrl: String         =
    s"$baseUrl$vapingDutyRoute/$contactPreferencesRoute/how-do-you-want-to-be-contacted"
  private val confirmYourPostalAddressUrl: String  =
    s"$baseUrl$vapingDutyRoute/$contactPreferencesRoute/review-confirm-address"
  private val postalAddressConfirmationUrl: String =
    s"$baseUrl$vapingDutyRoute/$contactPreferencesRoute/postal-address-confirmation"
  private val enterEmailAddressUrl: String         = s"$baseUrl$vapingDutyRoute/$contactPreferencesRoute/enter-email-address"
  private val emailUpdatedConfirmationUrl: String  =
    s"$baseUrl$vapingDutyRoute/$contactPreferencesRoute/email-confirmation"

  def saveCsrfToken(): CheckBuilder[RegexCheckType, String] = regex(_ => CsrfPattern).saveAs("csrfToken")

  def randomTestEmail(): String = {
    val formatter = DateTimeFormatter.ofPattern("ddMMmmss")
    val timestamp = LocalDateTime.now().format(formatter)
    s"autotest$timestamp@example.com"
  }

  val getAuthLoginPage: HttpRequestBuilder =
    http("Navigate to auth login stub page")
      .get(ggAuthSignInUrl)
      .check(status.is(200))
      .check(saveCsrfToken())

  def postAuthLoginPage(user: AuthUser, redirectUrl: String = doYouHaveApprovalIdUrl): HttpRequestBuilder =
    http("Login with user credentials")
      .post(ggAuthSignInUrl)
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("credentialStrength", "strong")
      .formParam("confidenceLevel", "50")
      .formParam("authorityId", "")
      .formParam("groupIdentifier", "")
      .formParam("email", "user@test.com")
      .formParam("credentialRole", "User")
      .formParam("redirectionUrl", redirectUrl)
      .formParam("affinityGroup", user.affinityGroup)
      .formParam("enrolment[0].state", user.enrolmentState)
      .formParam("enrolment[0].name", user.enrolmentKey)
      .formParam("enrolment[0].taxIdentifier[0].name", user.taxIdentifierName)
      .formParam("enrolment[0].taxIdentifier[0].value", user.taxIdentifierValue)
      .check(status.is(303))

  val getAuthSession: HttpRequestBuilder =
    http("get Auth Session")
      .get(authSessionUrl)
      .check(
        status.is(200),
        regex("""data-session-id="sessionId"[\s\S]*?<code[^>]*>(session-[^<]+)</code>""")
          .saveAs("sessionId"),
        regex("""data-session-id="authToken"[\s\S]*?<code[^>]*>[\s\S]*?(Bearer [A-Za-z0-9+/=]+)""")
          .saveAs("bearerToken")
      )

  def getPasscodes(email: String): HttpRequestBuilder =
    http("get passcodes")
      .get(s"$emailVerificationUrl/test-only/passcodes")
      .header("content-type", "application/x-www-form-urlencoded")
      .header("x-session-id", s => s("sessionId").as[String])
      .header("authorization", s => s("bearerToken").as[String])
      .check(
        status.is(200),
        jsonPath(s"$$.passcodes[?(@.email=='$email')].passcode")
          .ofType[String]
          .findAll
          .transform(_.lastOption.getOrElse(throw new RuntimeException(s"No passcode found for $email")))
          .saveAs("emailPasscode")
      )

  val navigateToVapingDutyPage: HttpRequestBuilder =
    http("Navigate to vaping duty Page")
      .get(s"$baseUrl/$vapingDutyRoute")
      .check(status.is(200))

  val getEnrolmentDoYouHaveAnApprovalIdPage: HttpRequestBuilder =
    http("Get Enrolment Approval Page")
      .get(doYouHaveApprovalIdUrl)
      .check(status.is(200))
      .check(saveCsrfToken())

  def postEnrolmentDoYouHaveAnApprovalIdPage(enrolmentApprovalQuestion: Boolean): HttpRequestBuilder =
    http("Post Enrolment Approval Page")
      .post(doYouHaveApprovalIdUrl)
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", enrolmentApprovalQuestion)
      .check(status.is(303))

  val getEnrolmentOrganisationSignInPage: HttpRequestBuilder =
    http("Get Enrolment Organisation Sign In Page")
      .get(organisationSignUrl)
      .check(status.is(200))

  val getYouNeedAnApprovalIDPage: HttpRequestBuilder =
    http("Get VPDID Approval Required Page")
      .get(youNeedAnApprovalIDUrl)
      .check(status.is(200))

  val getAlreadyEnrolledPage: HttpRequestBuilder =
    http("Get Already Enrolled Page")
      .get(alreadyEnrolledUrl)
      .check(status.is(200))

  val getHowDoYouWantToBeContactedPage: HttpRequestBuilder =
    http("Get How Do You Want To Be Contacted Page")
      .get(howDoYouWantToBeContactedUrl)
      .check(status.is(200))
      .check(css("input[name='csrfToken']", "value").saveAs("contactPrefCsrf"))

  def postHowDoYouWantToBeContactedPage(contactPreferenceRadioButton: String): HttpRequestBuilder =
    http("Post How Do You Want To Be Contacted Page")
      .post(howDoYouWantToBeContactedUrl)
      .formParam("csrfToken", "#{contactPrefCsrf}")
      .formParam("value", contactPreferenceRadioButton)
      .check(status.is(303))

  val getConfirmYourPostalAddressPage: HttpRequestBuilder =
    http("Get Confirm Your Postal Address Page")
      .get(confirmYourPostalAddressUrl)
      .check(status.is(200))

  val getPostalAddressConfirmationPage: HttpRequestBuilder =
    http("Get Postal Address Confirmation Page")
      .get(postalAddressConfirmationUrl)
      .check(status.is(200))

  val getWhatEmailAddressToBeContactedPage: HttpRequestBuilder =
    http("Get What Email Address To Be Contacted Page")
      .get(enterEmailAddressUrl)
      .check(status.is(200))

  def postWhatEmailAddressToBeContactedPage(emailAddress: String): HttpRequestBuilder =
    http("Post What Email Address To Be Contacted Page")
      .post(enterEmailAddressUrl)
      .formParam("csrfToken", "#{contactPrefCsrf}")
      .formParam("value", emailAddress)
      .check(status.is(303), header("Location").saveAs("emailVerificationRedirectUrl"))

  val getEmailConfirmationCodePage: HttpRequestBuilder =
    http("Get Email Confirmation Code Page")
      .get(session => session("emailVerificationRedirectUrl").as[String])
      .check(status.is(200))

  def postEmailConfirmationCodePage(): HttpRequestBuilder =
    http("Post Email Confirmation Code Page")
      .post(session => session("emailVerificationRedirectUrl").as[String])
      .formParam("csrfToken", "#{contactPrefCsrf}")
      .formParam("passcode", "#{emailPasscode}")
      .check(status.is(303))

  val getEmailAddressConfirmationPage: HttpRequestBuilder =
    http("Get Email Address Confirmation Page")
      .get(emailUpdatedConfirmationUrl)
      .check(status.is(200))

}
